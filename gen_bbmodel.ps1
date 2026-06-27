function New-UUID { [guid]::NewGuid().ToString() }

$F = @{}
function F { param($n) if (-not $F.ContainsKey($n)) { $F[$n] = New-UUID }; $F[$n] }

# Convert MC coord (Y-down) to BB coord (Y-up)
function mc2bb { param($mc) @($mc[0], -$mc[1], $mc[2]) }

# In MC: body part has addBox(cx,cy,cz, sx,sy,sz) and PartPose.offset(ox,oy,oz)
# Returns absolute MC from/to
function absCube { param($ox,$oy,$oz,$cx,$cy,$cz,$sx,$sy,$sz)
  $from = @($ox+$cx, $oy+$cy, $oz+$cz)
  $to = @($ox+$cx+$sx, $oy+$cy+$sy, $oz+$cz+$sz)
  @($from, $to)
}

# Body parts: all offsets and cubes from the Java source
$parts = @(
  @{name="body"; off=@(0,0,0); cube=@(-4,0,-2,8,12,4); uv=@(16,16)},
  @{name="head"; off=@(0,0,0); cube=@(-4,-8,-4,8,8,8); uv=@(0,0)},
  @{name="hat"; off=@(0,0,0); cube=@(-4.5,-8.5,-4.5,8,8,8); expand=$true; uv=@(32,0)},
  @{name="left_arm"; off=@(5,2,0); cube=@(-2,-2,-2,4,12,4); uv=@(44,22)},
  @{name="right_arm"; off=@(-5,2,0); cube=@(-2,-2,-2,4,12,4); uv=@(44,22); mirror=$true},
  @{name="left_leg"; off=@(2,12,0); cube=@(-2,0,-2,4,12,4); uv=@(0,16)},
  @{name="right_leg"; off=@(-2,12,0); cube=@(-2,0,-2,4,12,4); uv=@(0,16); mirror=$true}
)

$elements = @()
$outliner = @()

# Generate elements for main body parts
$bodyChildren = @()
$headChildren = @()
$sawBladeChildren = @()

foreach ($p in $parts) {
  $res = absCube $p.off[0] $p.off[1] $p.off[2] $p.cube[0] $p.cube[1] $p.cube[2] $p.cube[3] $p.cube[4] $p.cube[5]
  $mcFrom = $res[0]; $mcTo = $res[1]
  $bbFrom = @($mcFrom[0], (-$mcTo[1]), $mcFrom[2])
  $bbTo = @($mcTo[0], (-$mcFrom[1]), $mcTo[2])
  $origin = mc2bb $p.off
  $uuid = F $p.name

  # Round to 3 decimal places
  $bbFrom = $bbFrom | ForEach-Object { [Math]::Round($_,3) }
  $bbTo = $bbTo | ForEach-Object { [Math]::Round($_,3) }
  $origin = $origin | ForEach-Object { [Math]::Round($_,3) }

  $el = @{
    uuid = $uuid
    name = $p.name
    type = "cube"
    from = $bbFrom
    to = $bbTo
    origin = $origin
    rotation = @(0,0,0)
    box_uv = $true
    uv_offset = $p.uv
  }
  if ($p.expand) { $el.inflate = 0.5 }
  if ($p.mirror) { $el.mirror = $true }
  $elements += $el

  # Sort into outliner tree
  if ($p.name -eq "body") { $bodyChildren += $uuid }
  elseif ($p.name -eq "head") { $headChildren += $uuid }
  elseif ($p.name -eq "hat") { $headChildren += $uuid }
  elseif ($p.name -like "arm*") { $bodyChildren += $uuid }
  elseif ($p.name -like "leg*") { $bodyChildren += $uuid }
}

# --- Saw blade ---
# saw_blade in MC: PartPose.offset(0, -4, -4) relative to head (0,0,0)
$sawBladeOriginMC = @(0, -4, -4)
$sawBladeOriginBB = @($sawBladeOriginMC[0], (-$sawBladeOriginMC[1]), $sawBladeOriginMC[2]) | ForEach-Object { [Math]::Round($_,3) }

# blade_center in MC: PartPose.ZERO relative to saw_blade
# Cube: addBox(-0.5, -4, -4, 1, 8, 8) relative to saw_blade origin
$bcRes = absCube 0 0 0 -0.5 -4 -4 1 8 8  # relative to saw_blade (0,0,0)
$bcMCFrom = @($bcRes[0][0]+$sawBladeOriginMC[0], $bcRes[0][1]+$sawBladeOriginMC[1], $bcRes[0][2]+$sawBladeOriginMC[2])
$bcMCTo = @($bcRes[1][0]+$sawBladeOriginMC[0], $bcRes[1][1]+$sawBladeOriginMC[1], $bcRes[1][2]+$sawBladeOriginMC[2])
$bcBBFrom = @($bcMCFrom[0], (-$bcMCTo[1]), $bcMCFrom[2]) | ForEach-Object { [Math]::Round($_,3) }
$bcBBTo = @($bcMCTo[0], (-$bcMCFrom[1]), $bcMCTo[2]) | ForEach-Object { [Math]::Round($_,3) }

$el = @{
  uuid = F "blade_center"
  name = "blade_center"
  type = "cube"
  from = $bcBBFrom
  to = $bcBBTo
  origin = $sawBladeOriginBB
  rotation = @(0,0,0)
  box_uv = $true
  uv_offset = @(48, 32)
}
$elements += $el
$sawBladeChildren += (F "blade_center")

# Teeth
$toothRadius = 4.5
$deg = [Math]::PI / 180.0
for ($i = 0; $i -lt 8; $i++) {
  $angle = $i * 45.0 * $deg
  $ty = [Math]::Cos($angle) * $toothRadius
  $tz = [Math]::Sin($angle) * $toothRadius
  
  # In MC: PartPose.offset(0, ty, tz) relative to saw_blade
  # Cube: addBox(-0.5, -1.5, -1.5, 1, 3, 3)
  $tRes = absCube 0 0 0 -0.5 -1.5 -1.5 1 3 3
  $tMCFrom = @($tRes[0][0], $tRes[0][1]+$ty, $tRes[0][2]+$tz)
  $tMCTo = @($tRes[1][0], $tRes[1][1]+$ty, $tRes[1][2]+$tz)
  # Now offset by saw_blade origin
  $tMCFrom[0] += $sawBladeOriginMC[0]; $tMCFrom[1] += $sawBladeOriginMC[1]; $tMCFrom[2] += $sawBladeOriginMC[2]
  $tMCTo[0] += $sawBladeOriginMC[0]; $tMCTo[1] += $sawBladeOriginMC[1]; $tMCTo[2] += $sawBladeOriginMC[2]
  
  $tBBFrom = @($tMCFrom[0], (-$tMCTo[1]), $tMCFrom[2]) | ForEach-Object { [Math]::Round($_,3) }
  $tBBTo = @($tMCTo[0], (-$tMCFrom[1]), $tMCTo[2]) | ForEach-Object { [Math]::Round($_,3) }
  $tOriginMC = @(0, $ty, $tz)
  $tOriginMC[0] += $sawBladeOriginMC[0]; $tOriginMC[1] += $sawBladeOriginMC[1]; $tOriginMC[2] += $sawBladeOriginMC[2]
  $tOriginBB = @($tOriginMC[0], (-$tOriginMC[1]), $tOriginMC[2]) | ForEach-Object { [Math]::Round($_,3) }
  
  $uuid = F "tooth_$i"
  $el = @{
    uuid = $uuid
    name = "tooth_$i"
    type = "cube"
    from = $tBBFrom
    to = $tBBTo
    origin = $tOriginBB
    rotation = @(0,0,0)
    box_uv = $true
    uv_offset = @(48, 41)
  }
  $elements += $el
  $sawBladeChildren += $uuid
}

# Build outliner tree
$sawBladeUUID = F "saw_blade"
$headGroupUUID = F "head_group"
$bodyGroupUUID = F "body_group"
$rootUUID = F "root_group"

$headChildren += @{ type="group"; uuid=$sawBladeUUID; name="saw_blade"; origin=$sawBladeOriginBB; rotation=@(0,0,0); children=$sawBladeChildren }

$bodyChildren += @{ type="group"; uuid=$headGroupUUID; name="head"; origin=@(0,0,0); rotation=@(0,0,0); children=$headChildren }

# Root outliner
$outliner += @{
  type="group"
  uuid=$rootUUID
  name="root"
  origin=@(0,0,0)
  rotation=@(0,0,0)
  children = $bodyChildren
}

# Build the document
$doc = @{
  meta = @{
    format_version = "4.10"
    model_format = "Java Block/Entity"
    box_uv = $true
  }
  name = "crazed_illager"
  model_identifier = "goetypunk:crazed_illager"
  visible_box = @( -8, -24, -8, 16, 32, 16 )
  elements = $elements
  outliner = $outliner
  textures = @(@{
    name = "crazed_illager.png"
    id = (New-UUID)
    source = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAYdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjEuNvEZ80IAAAHISURBVGhD7ZoxbsMwDEXdC/T+BfQMPUJHjR4yduzYMWMnT548ZerUqWjXH4YpUqIkShIlJf0BDxYCy5L4aT6mtVqW5X6/3+
8IAAAA/vwKYJom13Vd1w0AAADAL4B9313XdQcAAADwC2BZVtd1HQAAAMAvgH3fXdd1BwAAAPALYFkW13UdAAAAwC+AZVlc1x0AAADAL4B5nl3XdQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAA
AAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAA
AMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/
AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AKY59l1XQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAAAAL8A5nl2XdcBAAAA/AIYhuF+v98BAAAA+PIVwDAMj8cDAAAA4MsXw3Vdh2F4PB4AAADAl+8P7vv+er0AAAAAvnwBvN/v6
YcPAAAAwBcBvN/vy7I83+83AAAAAF+8+Nd1fb1e27YNAAAA8M3rHwAAAPDlC2BZlvP5DAAAAMDXL4DH4zFN0+PxAAAAAPD1C+D1eo3j+Hq9AAAAAHj5B3N6+F8cwL9/AAAAAElFTkSuQmCC"
    width = 64
    height = 64
  })
}

$json = $doc | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText("D:\1.20.1-Architectury-example-main\src\main\resources\assets\goetypunk\crazed_illager.bbmodel", $json, [System.Text.Encoding]::UTF8)
Write-Host "bbmodel regenerated with Y-up coordinates"

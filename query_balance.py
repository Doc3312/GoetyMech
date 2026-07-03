import requests
import json

# DeepSeek API 余额查询
API_KEY = "your-api-key-here"  # 替换为你的 API Key
url = "https://api.deepseek.com/user/balance"
headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

try:
    response = requests.get(url, headers=headers)
    data = response.json()
    
    if response.status_code == 200:
        print("=== DeepSeek 账户余额查询 ===")
        print(f"账户状态: {'可用' if data.get('is_available') else '不可用'}")
        
        if 'balance_infos' in data:
            for info in data['balance_infos']:
                currency = info.get('currency', '未知')
                total = info.get('total_balance', '0')
                granted = info.get('granted_balance', '0')
                topped = info.get('topped_up_balance', '0')
                
                print(f"\n货币: {currency}")
                print(f"总余额: {total}")
                print(f"赠金余额: {granted}")
                print(f"充值余额: {topped}")
    else:
        print(f"查询失败，状态码: {response.status_code}")
        print(f"错误信息: {data}")
        
except Exception as e:
    print(f"查询出错: {e}")
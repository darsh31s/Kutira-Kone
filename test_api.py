import urllib.request
import json

req = urllib.request.Request(
    'https://generativelanguage.googleapis.com/v1beta/models?key=AIzaSyAQlmHkBvRpfm7EufWjky6aPa_JDth_dD8',
    headers={'Content-Type': 'application/json'}
)

try:
    with open("models.json", "w") as f:
        f.write(urllib.request.urlopen(req).read().decode('utf-8'))
    print("Success")
except Exception as e:
    print(e.read().decode('utf-8'))

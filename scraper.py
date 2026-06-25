import urllib.request, re, urllib.parse

items = {'Paracetamol': 'paracetamol', 'Vitamin C': 'vitamin-c', 'Amoxicillin': 'amoxicillin', 'Ibuprofen': 'ibuprofen', 'Cetirizine': 'cetirizine', 'Omeprazole': 'omeprazole', 'Promag': 'promag', 'Tolak Angin': 'tolak-angin', 'Betadine': 'betadine', 'Dexamethasone': 'dexamethasone', 'Diapet': 'diapet', 'Sangobion': 'sangobion'}

for name, q in items.items():
    try:
        url = 'https://www.k24klik.com/cari/' + q
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            html = response.read().decode('utf-8')
            match = re.search(r'data-src=\"(https://image\.k24klik\.com/v3/t/product/[^\"]+)\"', html)
            if match:
                print(name, '->', match.group(1))
            else:
                match = re.search(r'src=\"(https://image\.k24klik\.com/v3/t/product/[^\"]+)\"', html)
                if match:
                    print(name, '->', match.group(1))
    except Exception as e:
        print(name, '-> ERROR', e)

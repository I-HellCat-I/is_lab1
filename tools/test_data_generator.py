import json
import random
import yaml # pip install pyyaml

FILES_COUNT = 5
RECORDS_PER_FILE = 3500

GENRES = ['ACTION', 'DRAMA', 'ADVENTURE', 'TRAGEDY']
RATINGS = ['PG', 'R', 'NC_17']

# ID существующих сценаристов (предполагаем, что init.sql выполнен)
SCREENWRITER_IDS = [1, 2, 3, 4, 5]

def generate_movie(index):
    return {
        "name": f"Generated Movie {index}",
        "coordinatesX": random.randint(-100, 100),
        "coordinatesY": random.randint(-100, 100),
        "oscarsCount": random.randint(0, 10),
        "budget": random.randint(1000000, 100000000),
        "totalBoxOffice": random.randint(1000000, 500000000),
        "mpaaRating": random.choice(RATINGS),
        "directorId": random.choice(SCREENWRITER_IDS), # Используем тех же людей
        "screenwriterId": random.choice(SCREENWRITER_IDS),
        "operatorId": random.choice(SCREENWRITER_IDS),
        "length": random.randint(80, 200),
        "goldenPalmCount": random.randint(0, 5),
        "genre": random.choice(GENRES)
    }

def main():
    for i in range(1, FILES_COUNT + 1):
        data = [generate_movie(j) for j in range(RECORDS_PER_FILE)]

        # Чередуем форматы: JSON и YAML
        if i % 2 != 0:
            filename = f"large_import_{i}.json"
            with open(filename, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=None) # indent=None для уменьшения размера
        else:
            filename = f"large_import_{i}.yaml"
            with open(filename, 'w', encoding='utf-8') as f:
                yaml.dump(data, f, default_flow_style=False)

        print(f"Generated {filename} with {RECORDS_PER_FILE} records.")

def generate_very_large_import():
    for i in range(2):
        data = [generate_movie(j) for j in range(10**6)]

        # Чередуем форматы: JSON и YAML
        if i % 2 != 0:
            filename = f"very_large_import_{i}.json"
            with open(filename, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=None) # indent=None для уменьшения размера
        else:
            filename = f"very_large_import_{i}.yaml"
            with open(filename, 'w', encoding='utf-8') as f:
                yaml.dump(data, f, default_flow_style=False)

if __name__ == "__main__":
    # main()
    generate_very_large_import()
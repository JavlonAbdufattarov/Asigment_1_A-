import subprocess
from time import sleep
from random import randint

process = subprocess.Popen(['java', '-cp', '.', 'AStarMatrix'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

info = []
for i in range(9):
    info.append([""] * 9)

def check(x, y):
    a = 0
    res = []
    if info[x][y] == "S" or info[x][y] == "B" or info[x][y] == "K" or (info[x][y] == 'A' and not keyactivated):
        a += 1
        res.append(info[x][y])
    if (keyactivated):
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                if (0 <= x+dx <= 8 and 0 <= y+dy <= 8 and info[x+dx][y+dy] == 'S'):
                    a += 1
                    res.append("P")
                    return [a, res]
        i = 2
    else:
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                if (0 <= x+dx <= 8 and 0 <= y+dy <= 8 and info[x+dx][y+dy] == 'A'):
                    a += 1
                    res.append("P")
                    return [a, res]
        i = 1
    if (0 <= x-i <= 8 and info[x-i][y] == 'S' or 0 <= x+i <= 8 and info[x+i][y] == 'S' or 0 <= y-i <= 8 and info[x][y-i] == 'S' or 0 <= y+i <= 8 and info[x][y+i] == 'S'):
        a += 1
        res.append("P")
        return [a, res]

    return [a, res]


def show(neo_x, neo_y):
    with open('close.txt', encoding='utf-8', mode='w') as f1:
        a = list(map)
        a[map.find('𖨆')] = ' '
        a[map.find(str(neo_x) + " |") + 4 + neo_y * 4] = '𖨆'
        f1.write(''.join(a))

map = ''
keymaker_x = 0
keymaker_y = 0
key_x = 99
key_y = 99
with open('field.txt', encoding='utf-8') as initial:
    i = 1
    for line in initial:
        map += line
        if (3 <= i and i <= 19 and i % 2 == 1):
            line = line.split('|')[1:-1]
            for j in range(len(line)):
                if (line[j] == ' ■ '):
                    info[i // 2 - 1][j] = 'A'
                elif (line[j] == ' □ '):
                    info[i // 2 - 1][j] = 'S'
                elif (line[j] == ' ⚷ '):
                    info[i // 2 - 1][j] = 'B'
                    key_x = i // 2 - 1
                    key_y = j
                elif (line[j] == ' K '):
                    info[i // 2 - 1][j] = 'K'
                    keymaker_x = i // 2 - 1
                    keymaker_y = j
        i += 1

show(0, 0)
print('Введите режим (1 или 2):')
mode = int(input())
process.stdin.write(str(mode) + "\n" + str(keymaker_x) + " " + str(keymaker_y) + "\n")
process.stdin.flush()
now_x = 0
now_y = 0
keyactivated = False

while True:
    move = process.stdout.readline().strip()
    if 'e' in move:
        print(move)
        break
    move = move.split()
    now_x = int(move[1])
    now_y = int(move[2])
    if (now_x == key_x and now_y == key_y):
        keyactivated = True
        map = map.replace("■", " ").replace("⚷", " ")
    show(now_x, now_y)
    sleep(0.5)

    res = ''
    num = 0

    if mode == 1:
        left = -1
        right = 2
    else:
        left = -2
        right = 3

    for dx in range(left, right):
        for dy in range(left, right): 
            new_x = now_x + dx
            new_y = now_y + dy
            if 0 <= new_x <= 8 and 0 <= new_y <= 8:
                a = check(new_x, new_y)
                num += a[0]
                for elem in a[1]:
                    res += str(new_x) + " " + str(new_y) + " " + elem + "\n"

    process.stdin.write(str(num) + '\n' + res)
    process.stdin.flush()
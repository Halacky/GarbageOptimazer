
def DeleteToDo():
    FILE = open('C:\\Users\\RomeF\\OneDrive\\Docs\\GitHub\\GarbageOptimazer\\log.0.0.txt',"r", encoding="UTF-8")
    lines=[x.strip() for x in FILE if "февр" not in x]
    FILE.close()
    print(lines)
    FILE = open('C:\\Users\\RomeF\\OneDrive\\Docs\\GitHub\\GarbageOptimazer\\log.0.0.txt',"w",encoding="UTF-8")
    for x in lines:
        if x != "":
            FILE.write(x+'\n')

if __name__ == '__main__':
    DeleteToDo()


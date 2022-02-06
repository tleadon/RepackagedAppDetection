from pathlib import Path
import re
import pandas as pd
import matplotlib.pyplot as plt
import csv
import os
import glob
from dataclasses import dataclass, field

@dataclass
class Features:
    BroadcastReceiverOccurenceFrequency: int = 0 
    ActivityOccurenceFrequency: int = 0 
    ServicesOccurenceFrequency: int = 0 
    Permissions: int = 0
    MostCommonInternalSourceTargetPair:	str = "" 
    MostCommonExternalSourceTargetPair:	str = "" 
    MostCommonActivitySourceTargetPair:	str = "" 
    MostCommonServiceSourceTargetPair:	str = ""
    MostCommonBroadcastReceiverSourceTargetPair: str = ""
    MostCommonTargetComponent: str = "" 
    MostCommonSourceComponent: str = "" 
    Repacked: bool = False
    
    def CSVify(BroadcastReceiverOccurenceFrequency):
        return str(BroadcastReceiverOccurenceFrequency)

@dataclass(order=True)
class AppRows:
    appName: str
    featureData: list[Features] = field(default_factory=list)
    
    
def featuremaker(filename, appname):
    tdf = pd.read_csv(filename)
    #tdf.info()

    col = ['ICC Name', ' Source Component', ' Target Component', ' Type of Communication']
    iCCTypes = ['Activit', 'Service', 'Broadcast'] #Activit because of startActivities*
    typeComm = ['internal', 'external']
    
    ft = Features()
    entries = []
    entries.append(AppRows(appname, ft))
    
    #Now to find the occurence of each type
    count = 0
    while(count < len(tdf["ICC Name"].value_counts())):
        
        if iCCTypes[0] in tdf["ICC Name"].value_counts().index.tolist()[count]: #Activity
            entries[0].featureData.ActivityOccurenceFrequency += tdf["ICC Name"].value_counts()[count]
            
        elif iCCTypes[1] in tdf["ICC Name"].value_counts().index.tolist()[count]: #Services
            entries[0].featureData.ServicesOccurenceFrequency += tdf["ICC Name"].value_counts()[count]
            
        elif iCCTypes[2] in tdf["ICC Name"].value_counts().index.tolist()[count]: #Broadcast Recievers
            entries[0].featureData.BroadcastReceiverOccurenceFrequency += tdf["ICC Name"].value_counts()[count]
            
        else:
            print("")
            
        count += 1
    
    # print("_"*200)
    # print("Activity Count: ", entries[0].featureData.ActivityOccurenceFrequency)
    # print("Services Count: ", entries[0].featureData.ServicesOccurenceFrequency)
    # print("Broadcast Reciever Count: ", entries[0].featureData.BroadcastReceiverOccurenceFrequency)
    
    tdf['SourceTargetType'] = tdf[' Source Component'] + '-' + tdf[' Target Component'] + '-' + tdf[' Type of Communication']
    tdf['SourceTargetType'].value_counts()
    
    running = True
    count = 0
    while(count < len(tdf["SourceTargetType"].value_counts()) and running):
        if typeComm[0] in tdf["SourceTargetType"].value_counts().index.tolist()[count]: #internal
            entries[0].featureData.MostCommonInternalSourceTargetPair = tdf["SourceTargetType"].value_counts().index.tolist()[count]
            running = False
        count += 1
    
    running = True
    count = 0
    while(count < len(tdf["SourceTargetType"].value_counts()) and running):
        if typeComm[1] in tdf["SourceTargetType"].value_counts().index.tolist()[count]: #external
            entries[0].featureData.MostCommonExternalSourceTargetPair = tdf["SourceTargetType"].value_counts().index.tolist()[count]
            running = False
        count += 1
    
    # print("_"*200)
    # print("Most Common Internal Source-Target Pair: ", entries[0].featureData.MostCommonInternalSourceTargetPair)
    # print("Most Common External Source-Target Pair: ", entries[0].featureData.MostCommonExternalSourceTargetPair)
    
    #Now the same could but with the ICCTypes... I could probably make a function for these but anyway
    
    tdf['SourceTargetICC'] = tdf[' Source Component'] + '-' + tdf[' Target Component'] + '-' + tdf['ICC Name']
    # print("_"*200)
    
    running = True
    count = 0
    while(count < len(tdf["SourceTargetICC"].value_counts()) and running):
        if iCCTypes[0] in tdf["SourceTargetICC"].value_counts().index.tolist()[count]: #Activity
            entries[0].featureData.MostCommonActivitySourceTargetPair = tdf["SourceTargetICC"].value_counts().index.tolist()[count]
            running = False
        count += 1
        
    running = True
    count = 0
    while(count < len(tdf["SourceTargetICC"].value_counts()) and running):
        if iCCTypes[1] in tdf["SourceTargetICC"].value_counts().index.tolist()[count]: #Services
            entries[0].featureData.MostCommonServiceSourceTargetPair = tdf["SourceTargetICC"].value_counts().index.tolist()[count]
            running = False
        count += 1
        
    running = True
    count = 0
    while(count < len(tdf["SourceTargetICC"].value_counts()) and running):
        if iCCTypes[2] in tdf["SourceTargetICC"].value_counts().index.tolist()[count]: #Reciever
            entries[0].featureData.MostCommonBroadcastReceiverSourceTargetPair = tdf["SourceTargetICC"].value_counts().index.tolist()[count]
            running = False
        count += 1
    #I should definitely make these into a function lol
    
    # print("Most Common Activity Source-Target Pair: ", entries[0].featureData.MostCommonActivitySourceTargetPair)
    # print("Most Common Service Source-Target Pair: ", entries[0].featureData.MostCommonServiceSourceTargetPair)
    # print("Most Common Broadcast Reciever Source-Target Pair: ", entries[0].featureData.MostCommonBroadcastReceiverSourceTargetPair)
    # print("_"*200)
    
    permissions = tdf[' Permissions'].value_counts().index.tolist()[0]
    permArray = re.findall(r'\[(.*?)\]', permissions)
    #print("Number of Permissions:", len(permArray))
    entries[0].featureData.Permissions = len(permArray)
    
    entries[0].featureData.MostCommonTargetComponent = tdf[" Target Component"].value_counts().index.tolist()[0]
    entries[0].featureData.MostCommonSourceComponent = tdf[" Source Component"].value_counts().index.tolist()[0]
    
    #and finally, putting it in a csv.
    currentpath = str(Path().absolute())
    featureCSV = currentpath + "data.csv"
    featureArray = []
    featureArray.append(entries[0].appName)
    featureArray.append(entries[0].featureData.BroadcastReceiverOccurenceFrequency)
    featureArray.append(entries[0].featureData.ActivityOccurenceFrequency)
    featureArray.append(entries[0].featureData.ServicesOccurenceFrequency)
    featureArray.append(entries[0].featureData.Permissions)
    featureArray.append(entries[0].featureData.MostCommonInternalSourceTargetPair)
    featureArray.append(entries[0].featureData.MostCommonExternalSourceTargetPair)
    featureArray.append(entries[0].featureData.MostCommonActivitySourceTargetPair)
    featureArray.append(entries[0].featureData.MostCommonServiceSourceTargetPair)
    featureArray.append(entries[0].featureData.MostCommonBroadcastReceiverSourceTargetPair)
    featureArray.append(entries[0].featureData.MostCommonTargetComponent)
    featureArray.append(entries[0].featureData.MostCommonSourceComponent)
    featureArray.append(entries[0].featureData.Repacked)
    
    #I'm almost 100% sure I could've made this into a loop or a seperate function lol this doesnt need to be in main bro ill fix it later
    
    #for i in featureArray:
    #    print(i)
    
    csvheader = [
        'App Name',
        'Broadcast Receiver Occurence Frequency',
        'Activity Occurence Frequency',
        'Services Occurence Frequency',
        'Permissions',
        'Most Common Internal Source-Target Pair',
        'Most Common External Source-Target Pair',
        'Most Common Activity Source-Target Pair',
        'Most Common Service Source-Target Pair',
        'Most Common Broadcast Receiver Source-Target Pair',
        'Most Common Target Component',
        'Most Common Source Component',
        'RepackStatus'
        ]
    
    write_header = not os.path.exists(featureCSV)
    with open(featureCSV, 'a+', encoding='UTF8', newline='') as f:
        writer = csv.writer(f)
        if write_header:
            writer.writerow(csvheader)
        writer.writerow(featureArray)
        
def main():
    path = "../apkCSVFiles"
    csv_files = glob.glob(os.path.join(path,"*.csv"))
    
    for f in csv_files:
        print(f)
        an = f.split("\\")[-1]
        featuremaker(f, an)
        
if __name__ == "__main__":
    main()
    
    
    # broadcast = [
    # "sendBroadcast",
    # "sendBroadcastAsUser",
    # "sendOrderedBroadcast",
    # "sendOrderedBroadcastAsUser",
    # "sendStickyBroadcast",
    # "sendStickyBroadcastAsUser",
    # "sendStickyOrderedBroadcast",
    # "sendStickyOrderedBroadcastAsUser" ]
    # activity = [
    # "startActivities",
    # "startActivity", 
    # "startActivityForResult",
    # "startActivityFromChild",
    # "startActivityFromFragment",
    # "startActivityIfNeeded" ]
    # service = [
    # "startService",
    # "bindService" ]
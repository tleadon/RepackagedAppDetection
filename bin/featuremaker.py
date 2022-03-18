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
    BroadcastReceiverOccurenceFrequency: int = 0 #
    ActivityOccurenceFrequency: int = 0 #
    ServicesOccurenceFrequency: int = 0 #
    TotalComponentFrequency: int = BroadcastReceiverOccurenceFrequency + ActivityOccurenceFrequency + ServicesOccurenceFrequency 
    MostCommonTargetComponent: int = 0 #
    MostCommonSourceComponent: int = 0 #
    InternalOccurence: int = 0 #
    ExternalOccurence: int = 0 #
    Permissions: int = 1 #
    NormalPerms: int = 0 #
    DangerousPerms: int = 0 #
    SignaturePerms: int = 0 #
    RiskRatePerPerms: int = DangerousPerms / Permissions
    androidApiCount: int = 0 #
    javaApiCount: int = 0 #
    totalApiCount: int = androidApiCount + javaApiCount
    try:
        apiPerComponent: int = totalApiCount / TotalComponentFrequency
    except ZeroDivisionError:
        apiPerComponent = 0
    userActionCount: int = 0 #
    try: 
        userActionPerComponent: int = userActionCount / TotalComponentFrequency
    except ZeroDivisionError:
        userActionPerComponent = 0
    StringOffset: bool = False #notinorder?
    Repacked: bool = False 
    
    
    def calc(self):
        self.TotalComponentFrequency = self.BroadcastReceiverOccurenceFrequency + self.ActivityOccurenceFrequency + self.ServicesOccurenceFrequency
        try:
            self.RiskRatePerPerms = self.DangerousPerms / self.Permissions
        except ZeroDivisionError:
            self.RiskRatePerPerms = 0
        self.totalApiCount = self.androidApiCount + self.javaApiCount
        try:
            self.apiPerComponent = self.totalApiCount / self.TotalComponentFrequency
        except ZeroDivisionError:
            self.apiPerComponent = 0
        try:
            self.userActionPerComponent = self.userActionCount / self.TotalComponentFrequency
        except:
            self.userActionPerComponent = 0
        

@dataclass(order=True)
class AppRows:
    appName: str
    featureData: list[Features] = field(default_factory=list)
    
    
def featuremaker(filename, appname):
    
    try:
        tdf = pd.read_csv(filename)
    except:
        print("Oops")
        return
    

    col = ['ICC Name', ' Source Component', ' Target Component', ' Type of Communication']
    iCCTypes = ['Activit', 'Service', 'Broadcast'] #Activit because of startActivities*
    typeComm = ['internal', 'external']
    normalperms = [
        'ACCESS_BLOBS_ACROSS_USERS','ACCESS_CHECKIN_PROPERTIES','ACCESS_LOCATION_EXTRA_COMMANDS',
        'ACCESS_NETWORK_STATE','BLUETOOTH',
        'BLUETOOTH_ADMIN','BROADCAST_STICKY',
        'CALL_COMPANION_APP','CHANGE_NETWORK_STATE',
        'CHANGE_WIFI_MULTICAST_STATE','CHANGE_WIFI_STATE',
        'DELIVER_COMPANION_MESSAGES','DISABLE_KEYGUARD',
        'EXPAND_STATUS_BAR','FOREGROUND_SERVICE',
        'GET_PACKAGE_SIZE','HIDE_OVERLAY_WINDOWS',
        'HIGH_SAMPLING_RATE_SENSORS','INSTALL_SHORTCUT',
        'INTERACT_ACROSS_PROFILES','INTERNET',
        'KILL_BACKGROUND_PROCESSES','LAUNCH_MULTI_PANE_SETTINGS_DEEP_LINK',
        'LOADER_USAGE_STATS','MANAGE_OWN_CALLS',
        'MODIFY_AUDIO_SETTINGS','NFC','NFC_PREFERRED_PAYMENT_INFO',
        'NFC_TRANSACTION_EVENT','QUERY_ALL_PACKAGES',
        'READ_BASIC_PHONE_STATE','READ_SYNC_SETTINGS',
        'READ_SYNC_STATS','RECEIVE_BOOT_COMPLETED',
        'REORDER_TASKS','REQUEST_COMPANION_PROFILE_WATCH',
        'REQUEST_COMPANION_RUN_IN_BACKGROUND',
        'REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND',
        'REQUEST_COMPANION_USE_DATA_IN_BACKGROUND','REQUEST_DELETE_PACKAGES',
        'REQUEST_IGNORE_BATTERY_OPTIMIZATIONS','REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE',
        'REQUEST_PASSWORD_COMPLEXITY','SCHEDULE_EXACT_ALARM',
        'SET_ALARM', 'SET_WALLPAPER_HINTS','SET_WALLPAPER',
        'TRANSMIT_IR','UPDATE_PACKAGES_WITHOUT_USER_ACTION', 
        'USE_BIOMETRIC','USE_FULL_SCREEN_INTENT','VIBRATE','WRITE_SYNC_SETTINGS']
    dangerousperms = [
        'ACCEPT_HANDOVER',
        'ACCESS_BACKGROUND_LOCATION',
        'ACCESS_COARSE_LOCATION',
        'ACCESS_FINE_LOCATION',
        'ACCESS_MEDIA_LOCATION',
        'ACCESS_NOTIFICATION_POLICY',
        'ACCESS_WIFI_STATE',
        'ACTIVITY_RECOGNITION',
        'ADD_VOICEMAIL',
        'ANSWER_PHONE_CALLS',
        'BLUETOOTH_ADVERTISE',
        'BLUETOOTH_CONNECT',
        'BLUETOOTH_SCAN',
        'BODY_SENSORS',
        'BODY_SENSORS_BACKGROUND',
        'CALL_PHONE',
        'CAMERA',
        'GET_ACCOUNTS',
        'NEARBY_WIFI_DEVICES',
        'POST_NOTIFICATIONS',
        'PROCESS_OUTGOING_CALLS',
        'READ_CALENDAR',
        'READ_CALL_LOG',
        'READ_CONTACTS',
        'READ_EXTERNAL_STORAGE',
        'READ_PHONE_NUMBERS',
        'READ_PHONE_STATE',
        'READ_NEARBY_STREAMING_POLICY',
        'READ_SMS',
        'RECEIVE_SMS',
        'RECEIVE_MMS',
        'RECEIVE_WAP_PUSH',
        'RECORD_AUDIO',
        'SEND_SMS',
        'USE_SIP',
        'UWB_RANGING',
        'WAKE_LOCK',
        'WRITE_CALENDAR',
        'WRITE_CALL_LOG',
        'WRITE_CONTACTS',
        'WRITE_EXTERNAL_STORAGE',
    ]
    signatureperms = [
        'BATTERY_STATS',
        'BIND_ACCESSIBILITY_SERVICE',
        'BIND_AUTOFILL_SERVICE',
        'BIND_CALL_REDIRECTION_SERVICE',
        'BIND_CARRIER_MESSAGING_CLIENT_SERVICE',
        'BIND_CARRIER_SERVICES',
        'BIND_CONDITION_PROVIDER_SERVICE',
        'BIND_CONTROLS',
        'BIND_DEVICE_ADMIN',
        'BIND_DREAM_SERVICE',
        'BIND_INCALL_SERVICE',
        'BIND_INPUT_METHOD',
        'BIND_MIDI_DEVICE_SERVICE',
        'BIND_NFC_SERVICE',
        'BIND_NOTIFICATION_LISTENER_SERVICE',
        'BIND_PRINT_SERVICE',
        'BIND_QUICK_ACCESS_WALLET_SERVICE',
        'BIND_QUICK_SETTINGS_TILE',
        'BIND_REMOTEVIEWS',
        'BIND_SCREENING_SERVICE',
        'BIND_TELECOM_CONNECTION_SERVICE',
        'BIND_TEXT_SERVICE',
        'BIND_TV_INPUT',
        'BIND_VISUAL_VOICEMAIL_SERVICE',
        'BIND_VOICE_INTERACTION',
        'BIND_VPN_SERVICE',
        'BIND_VR_LISTENER_SERVICE',
        'BIND_WALLPAPER',
        'CHANGE_CONFIGURATION',
        'CLEAR_APP_CACHE',
        'DELETE_CACHE_FILES',
        'GET_ACCOUNTS_PRIVILEGED',
        'GLOBAL_SEARCH',
        'INSTANT_APP_FOREGROUND_SERVICE',
        'LOADER_USAGE_STATS',
        'MANAGE_MEDIA',
        'PACKAGE_USAGE_STATS',
        'READ_VOICEMAIL',
        'REQUEST_INSTALL_PACKAGES',
        'START_VIEW_APP_FEATURES',
        'START_VIEW_PERMISSION_USAGE',
        'SYSTEM_ALERT_WINDOW',
        'USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER',
        'WRITE_VOICEMAIL',
        'WRITE_SETTINGS',
    ]
    
    
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
    
    if(tdf[' Permissions'].value_counts().index.tolist()):
        permissions = tdf[' Permissions'].value_counts().index.tolist()[0]
        permArray = re.findall(r'\[(.*?)\]', permissions)
        for perm in permArray:
            for normals in normalperms:
                if normals in perm:
                    entries[0].featureData.NormalPerms += 1;
                    #print("Normal Permission Count: ", entries[0].featureData.NormalPerms)
            for danger in dangerousperms:
                if danger in perm:
                    entries[0].featureData.DangerousPerms += 1;
                    #print("Dangerous Permission Count: ", entries[0].featureData.DangerousPerms)
            for normals in normalperms:
                if normals in perm:
                    entries[0].featureData.SignaturePerms += 1;
                #print("Signature Permission Count: ", entries[0].featureData.SignaturePerms)
        #print("Number of Permissions:", len(permArray))
        entries[0].featureData.Permissions = len(permArray)
    
    entries[0].featureData.androidApiCount = tdf[' Android API Count'].value_counts()[0]
    entries[0].featureData.javaApiCount = tdf[' Java API Count'].value_counts()[0]
    entries[0].featureData.userActionCount = tdf[' User Action Count'].value_counts()[0]

    entries[0].featureData.MostCommonTargetComponent = tdf[" Target Component"].value_counts()[0]
    entries[0].featureData.MostCommonSourceComponent = tdf[" Source Component"].value_counts()[0]
    
    
    if(tdf[' Type of Communication'].value_counts().index.tolist()[0] == "external"):
        entries[0].featureData.ExternalOccurence = tdf[' Type of Communication'].value_counts()[0]
        entries[0].featureData.InternalOccurence = tdf[' Type of Communication'].value_counts()[1]
        
    if(tdf[' Type of Communication'].value_counts().index.tolist()[0] == "internal"):
        entries[0].featureData.ExternalOccurence = tdf[' Type of Communication'].value_counts()[1]
        entries[0].featureData.InternalOccurence = tdf[' Type of Communication'].value_counts()[0]
        
    if(tdf[' SSoutO'].value_counts().index.tolist()[0] == 1):
        entries[0].featureData.StringOffset = True
    
    if(tdf[' Repacked'].value_counts().index.tolist()[0] == 1):
        entries[0].featureData.Repacked = True
    
    #and finally, putting it in a csv.
    entries[0].featureData.calc()
    
    currentpath = str(Path().absolute())
    featureCSV = currentpath + "data.csv"
    featureArray = []
    featureArray.append(entries[0].appName)
    featureArray.append(entries[0].featureData.BroadcastReceiverOccurenceFrequency)
    featureArray.append(entries[0].featureData.ActivityOccurenceFrequency)
    featureArray.append(entries[0].featureData.ServicesOccurenceFrequency)
    featureArray.append(entries[0].featureData.TotalComponentFrequency)
    featureArray.append(entries[0].featureData.MostCommonTargetComponent)
    featureArray.append(entries[0].featureData.MostCommonSourceComponent)
    featureArray.append(entries[0].featureData.InternalOccurence)
    featureArray.append(entries[0].featureData.ExternalOccurence)
    featureArray.append(entries[0].featureData.Permissions)
    featureArray.append(entries[0].featureData.NormalPerms)
    featureArray.append(entries[0].featureData.DangerousPerms)
    featureArray.append(entries[0].featureData.SignaturePerms)
    featureArray.append(entries[0].featureData.RiskRatePerPerms)
    featureArray.append(entries[0].featureData.androidApiCount)
    featureArray.append(entries[0].featureData.javaApiCount)
    featureArray.append(entries[0].featureData.totalApiCount)
    featureArray.append(entries[0].featureData.apiPerComponent)
    featureArray.append(entries[0].featureData.userActionCount)
    featureArray.append(entries[0].featureData.userActionPerComponent)
    featureArray.append(entries[0].featureData.StringOffset)
    featureArray.append(entries[0].featureData.Repacked) 
    
    #I'm almost 100% sure I could've made this into a loop or a seperate function lol this doesnt need to be in main bro ill fix it later    
    
    csvheader = [
        'AppName',
        'BroadcastReceiverOccurenceFrequency',
        'ActivityOccurenceFrequency:',
        'ServicesOccurenceFrequency:',
        'TotalComponentFrequency: ',
        'MostCommonTargetComponent: ',
        'MostCommonSourceComponent: ',
        'InternalOccurence:',
        'ExternalOccurence',
        'Permissions',
        'NormalPerms',
        'DangerousPerms',
        'SignaturePerms',
        'RiskRatePerPerms',
        'androidApiCount',
        'javaApiCount',
        'totalApiCount',
        'apiPerComponent',
        'userActionCount',
        'userActionPerComponent',
        'StringOffset',
        'Repacked'
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;



public class App {

    public static String[] methods = {
    "sendBroadcast",
    "sendBroadcastAsUser",
    "sendOrderedBroadcast",
    "sendOrderedBroadcastAsUser",
    "sendStickyBroadcast",
    "sendStickyBroadcastAsUser",
    "sendStickyOrderedBroadcast",
    "sendStickyOrderedBroadcastAsUser",
    "startActivities",
    "startActivity", 
    "startActivityForResult",
    "startActivityFromChild",
    "startActivityFromFragment",
    "startActivityIfNeeded",
    "startService",
    "bindService"
    };

    public static String[] xmlandroid = {
        "activity",
        "service",
        "reciever",
        "intent-filter",
        "android:name"
    };

    public static void main(String[] args) throws Exception {

        //File apps = new File("C:/ApkTool/smali-apps/");
        File apps = new File("C:/Users/ttlea/OneDrive/Documents/Thesis 1/apktool/apps/");
        File[] appsArray = apps.listFiles();
        for(File file : appsArray ){
            if(file.isDirectory()){
                if (file.getName().contains("originals")){
                    Boolean repacked = false;
                    generator(file, repacked);
                }
                else if (file.getName().contains("repacks")){
                    Boolean repacked = true;
                    generator(file, repacked);
                }
                else {
                    System.out.println(file.getName() + " not analyzed.");
                }
            }
        }
        
    }

    public static void generator(File file, Boolean repacked) throws IOException, InterruptedException{
        String folderpath = file.getAbsolutePath();
        File folderfile = new File(folderpath);
        File[] apps = folderfile.listFiles();
        for(File app : apps){
            if(app.isDirectory()){
                String appName = app.getAbsolutePath();
                System.out.println(appName);
                File appfile = new File(appName);
                File[] appFiles = appfile.listFiles();
                String manifestName = slashToslash(appName) + "/AndroidManifest.xml";
                File manifest = new File(manifestName);
                csvMaker(appFiles, manifest, repacked);
            }
        }
    }

    public static void csvMaker(File[] appFiles, File manifest, Boolean repacked) throws IOException, InterruptedException{
        ArrayList<iCComp> iCCList = new ArrayList<iCComp>();
        Scanner maniscanny = new Scanner(manifest);
        String manicurrent;
        String[] appname = {"appCSV"};
        while(maniscanny.hasNextLine()){
            manicurrent = maniscanny.nextLine();
            if(manicurrent.contains("<application") && manicurrent.contains("android:name")){
                appname = StringUtils.substringsBetween(manicurrent, "android:name=\"", "\"");
                if(appname[0] != "appCSV"){ //ensuring it changes from the default name appCSV
                    break;
                }
                
            }
            
        }
        if(appname[0] == "appCSV"){
            appname[0] = RandomStringUtils.randomAlphanumeric(12);
        };
        maniscanny.close();

        String apkCSVFolderPath = System.getProperty("user.dir") + "\\apkCSVFiles\\";
        File apkCSVFolder = new File(apkCSVFolderPath);
        apkCSVFolder.mkdir();

        String appCSV = apkCSVFolderPath + appname[0] + ".csv";
        File iccCSV = new File(appCSV);
        if (iccCSV.createNewFile()) {
              System.out.println("File created: " + iccCSV.getName());
        }
        FileWriter csvWriter = new FileWriter(iccCSV);

        csvWriter.write("ICC Name, Source Component, Target Component, Type of Communication, Android API Count, Java API Count, User Action Count, Permissions, SSoutO, Repacked\n");
        String appPerm = getPermissions(manifest);
        String rp = repackedornah(repacked);
        String ssoNotInOrder = androsoo(appFiles);
        csvWriter.write(" , , , , , , ," + appPerm + "," + ssoNotInOrder + "," + rp + "\n");

        HashMap<String, Integer> apiCount = new HashMap<String, Integer>();
        apiCount.put("android", 0);
        apiCount.put("java", 0);
        apiCount.put("useraction", 0);
        csvFinisher(csvWriter, appFiles, iCCList, manifest, apiCount);        
        csvWriter.close();
    }

    public static String androsoo(File[] appFiles) throws IOException, InterruptedException{
        for (File file : appFiles){
            if(file.isFile()){
                String filename = file.toString();
                int index = filename.lastIndexOf('.');
                if(index > 0){
                    String extension = filename.substring(index + 1);
                    if(extension.contains("dex")){
                        String command = "\"C:\\Users\\ttlea\\OneDrive\\Documents\\Thesis 1\\ICC Mapper\\androsoo.exe\" " + 
                        "\"" + filename + "\"";
                        System.out.println(command);
                        Process process = Runtime.getRuntime().exec(command);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line ="";
                        while((line = reader.readLine()) != null){
                            
                            if(line.contains("String offset not")){
                                System.out.println(line);
                                 return "1";
                            }
                        }

                    }
                }
            }
        }
        
        
        return "0";
    }

    public static String repackedornah(Boolean repacked){
        if(repacked){
            return "1";
        }
        else{
            return "0";
        }
    }

    public static String getPermissions(File manifest) throws FileNotFoundException {
        Scanner maniscanny = new Scanner(manifest);
        String current;
        String[] currentpermission;
        String permissions = "";
        while(maniscanny.hasNextLine()){
            current = maniscanny.nextLine();
            if(current.contains("uses-permission") && current.contains("android:name")){
                currentpermission = StringUtils.substringsBetween(current, "android:name=\"", "\"");
                permissions += Arrays.toString(currentpermission) + "-";
            }
        }

        maniscanny.close();
        return permissions;
    }

    public static void csvFinisher(FileWriter csvWriter, File[] appFiles, ArrayList<iCComp> iCCList, File manifest, HashMap<String, Integer> apiCount) throws IOException{

        for (File file : appFiles){
            if (file.isDirectory() && file.getAbsolutePath().contains("smali")){
                File[] subDirectory = file.listFiles();
                csvFinisher(csvWriter, subDirectory, iCCList, manifest, apiCount);
            }
            if (file.isFile()){
                iCCList = iccFinder(file, manifest, apiCount);
                for (iCComp i: iCCList){
                    csvWriter.append(i.toCSVFormat());
                }
            }
        }
    }

    public static ArrayList<iCComp> iccFinder(File file, File manifest, HashMap<String, Integer> apiCount) throws FileNotFoundException{

            ArrayList<iCComp> iCCList = new ArrayList<iCComp>();
            apifeatures apifeatures = new apifeatures();
            Scanner scannah = new Scanner(file);
            String[] currentSource = null;
            String[] currentTarget = {"null"};
            int count = 0;

            while(scannah.hasNextLine()){

                String current = scannah.nextLine();
                    
                if (current.contains(".class")){ //.class [stuff] L [sourceComponent];
                    currentSource = StringUtils.substringsBetween(current, " L", ";");
                }
                if (current.contains("new-instance") && current.contains("Intent")){
                    Boolean done = false;
                    while(!done){
                        
                        current = scannah.nextLine();
                        if (current.contains("const-class")){
                            currentTarget = StringUtils.substringsBetween(current, ", L", ";");
                            break;
                        }
                        if (current.contains("const-string")){
                            currentTarget = StringUtils.substringsBetween(current, "\"", "\"");
                            break;
                        }
                        if (current.contains(".end method") || current.contains("return")){
                            done = true;
                        }
                    }

                }
                if(current.contains("Landroid/") || current.contains("Lorg/")){
                    for(String api : apifeatures.androidAPIs){
                        if (current.contains(api)){
                            apiCount.put("android", apiCount.get("android") + 1);
                            break;
                        }
                    }
                }
                
                if(current.contains("Ljava/")){
                    for(String api : apifeatures.javaAPIs){
                        if (current.contains(api)){
                            apiCount.put("java", apiCount.get("java") + 1);
                            break;
                        }
                    }
                }

                for(String userAction : apifeatures.userActionList){
                    if(current.contains(userAction)){
                        apiCount.put("useraction", apiCount.get("useraction") + 1);
                    }
                }
                    
                for(String i : methods){
                    if(current.contains(i) && !current.contains(".method")){
                        iCCList.add(new iCComp(i));
                        if(!Objects.isNull(currentSource) && !Objects.isNull(currentTarget)){
                        iCCList.get(count).setSourceComp(currentSource[0]);
                        iCCList.get(count).setTargetComp(currentTarget[0]);
                        iCCList.get(count).setTypeComm(findTypeComm(manifest, iCCList.get(count).getSourceComp()));
                        iCCList.get(count).androidAPICount(apiCount.get("android"));
                        iCCList.get(count).javaAPICount(apiCount.get("java"));
                        iCCList.get(count).userICount(apiCount.get("useraction"));
                        count++;
                        }
                    }
                }
            }
            scannah.close();
            return iCCList;
    }

    public static String findTypeComm(File manifestFile, String sourceComp) throws FileNotFoundException{
        String typeComm = "external";
        String androidname = "android:name=\"" + slashToDot(sourceComp);
        String[] internalArrayChecker = StringUtils.substringsBetween(slashToDot(sourceComp), ".", ".");
        int probablyInternal = 0;
        String current;
        
        Scanner scanner = new Scanner(manifestFile);
        while(scanner.hasNextLine()){
            current = scanner.nextLine();
            if(current.contains(androidname)){
                while(!current.contains("</intent-filter>") && !current.contains("</manifest>") && scanner.hasNextLine()){
                    current = scanner.nextLine();

                    if(!Objects.isNull(internalArrayChecker))
                        for(String i : internalArrayChecker){
                            
                            if(current.contains(i)){
                                probablyInternal++;
                                
                            }
                            if(probablyInternal >= 1){
                                typeComm = "internal";
                            }
                        }
                    probablyInternal = 0;
                }
                scanner.close();
                return typeComm;
            }
        }
        scanner.close();
        return typeComm;
    }

    public static String slashToDot(String slashy){
        String dotty = StringUtils.replaceChars(slashy, "/", ".");
        return dotty;
    }

    public static String slashToslash(String slashy){
        String dotty = StringUtils.replaceChars(slashy, "\\", "/");
        return dotty;
    }
    
    /* public static void iccFinderForSourceCode(File file) throws FileNotFoundException{
        try{
            ArrayList<iCComp> iCCList = new ArrayList<iCComp>();
            int count = 0;
            String currentTarget = null;
            Scanner scannah = new Scanner(file);
            while(scannah.hasNextLine()){
                String current = scannah.nextLine();
                if(current.contains(intentRelated[0]) && current.contains(intentRelated[1]) && current.contains(intentRelated[2])){ //new Intent + this, + .class
                    currentTarget = StringUtils.substringBetween(current, intentRelated[1], intentRelated[2]);
                    //System.out.println(current);
                    System.out.println(currentTarget);
                }
                if(current.contains(intentRelated[4]) && current.contains(intentRelated[5])){ //setClassName + getPackageName()
                    currentTarget = StringUtils.substringBetween(current, (intentRelated[5] + "\""), ");");
                    if(currentTarget.contains("\").")){
                        //which means it has some .setAction or something added onto the back like: 
                        //intent = (new Intent()).setClassName(getPackageName(), "com.whatsapp.gifvideopreview.GifVideoPreviewActivity").putExtra("preview_media_url", str4).putExtra("media_url", str5);
                        //or new Intent()).setClassName(getPackageName(), "com.whatsapp.HomeActivity").setAction("com.whatsapp.intent.action.CHATS");
                        //and I don't need that for this stage of my coding Lol
                        //So, in hte first case we'd have: "com.whatsapp.gifvideopreview.GifVideoPreviewActivity").putExtra("preview_media_url", str4).putExtra("media_url", str5
                        //and in the second case we'd have: "com.whatsapp.HomeActivity").setAction("com.whatsapp.intent.action.CHATS"
                        String currentTargettemp = currentTarget;
                        System.out.println(currentTarget);
                        currentTarget = StringUtils.substringBefore(currentTargettemp, "\").");
                        
                        //toString because StringUtils.substringsBetween returns String array
                        //So, this would be: 
                        //1: com.whatsapp.gifvideopreview.GifVideoPreviewActivity, 2: com.whatsapp.HomeActivity
                    }
                    //System.out.println(current);
                    System.out.println(currentTarget);
                }

                if(current.contains(intentRelated[4]) && (!(current.contains(intentRelated[2])) || !(current.contains(intentRelated[1])))){ 
                    //setClassName + no .class :( which is basically getPackageName() but they didn't use the method so, usually like:
                    //intent.setClassName("com.twitter.android", "com.twitter.android.ProfileActivity");
                    currentTarget = StringUtils.substringBetween(current, ", \"", "\");");
                    //System.out.println(current);
                    System.out.println(currentTarget);
                }


                for(String i : methods){
                    if(current.contains(i)){

                        iCCList.add(new iCComp(i));
                        iCCList.get(count).setTargetComp(currentTarget);
                        System.out.println(iCCList.get(count).getICCName());
                        count++;

                    }
                }
                
            }
            scannah.close();
        } catch (FileNotFoundException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
        }
    } */

















 /*    //extract the jar file + make a folder for the app's contents
    public static String unzip(File archive, File destDir, String appName) throws IOException {
        byte[] buffer = new byte[256 * 1024];
        String newDestDir = destDir.getAbsolutePath();
        appName = StringUtils.substringBefore(appName, ".");
        newDestDir = newDestDir + "\\" + appName;
        System.out.println(newDestDir);
        File appFolder = new File (newDestDir);
        appFolder.mkdirs();
        try (JarFile jar = new JarFile(archive)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry ent = entries.nextElement();
                File f = new File(appFolder, ent.getName());
                if (ent.isDirectory()) {
                    f.mkdir();
                    continue;
                }
                try (InputStream is = jar.getInputStream(ent);
                     FileOutputStream os = new FileOutputStream(f)) {
                    for (int r; (r = is.read(buffer)) > 0; ) {
                        os.write(buffer, 0, r);
                    }
                }
            }
        }
        return newDestDir;
    } */

/*     //I'm using jd-core api to decompile the class files
    public static void deClassifier () throws Exception{
        final DecompilerSettings settings = DecompilerSettings.javaDefaults();

        try (final FileOutputStream stream = new FileOutputStream("C:\\Users\\ttlea\\OneDrive\\Documents\\Thesis 1\\Apps\\sloppyb-dex2jar\\id\\beeper\\sloppybird\\a\\c.class");
             final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
        
            Decompiler.decompile(
                "sloppyb-dex2jar\\id\\beeper\\sloppybird\\a\\c",
                new PlainTextOutput(writer),
                settings
            );
        }
        catch (final IOException e) {
            System.out.println(e);
        }
        */
    }




import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
                String appName = file.getAbsolutePath();
                System.out.println(appName);
                File app = new File(appName);
                File[] appFiles = app.listFiles();
                String manifestName = slashToslash(appName) + "/AndroidManifest.xml";
                System.out.println(manifestName);
                File manifest = new File(manifestName);
                csvMaker(appFiles, manifest);
            }
        }
        
    }

    public static void csvMaker(File[] appFiles, File manifest) throws IOException{
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
            appname[0] = RandomStringUtils.randomAlphanumeric(64);
        };
        maniscanny.close();

        String apkCSVFolderPath = System.getProperty("user.dir") + "\\apkCSVFiles\\";
        File apkCSVFolder = new File(apkCSVFolderPath);
        apkCSVFolder.mkdir();

        String appCSV = apkCSVFolderPath + appname[0] + ".csv";
        File iccCSV = new File(appCSV);
        FileWriter csvWriter = new FileWriter(iccCSV);
        if (iccCSV.createNewFile()) {
              System.out.println("File created: " + iccCSV.getName());
        }
        
        csvWriter.write("ICC Name, Source Component, Target Component, Type of Communication, Permissions\n");
        String appPerm = getPermissions(manifest);
        csvWriter.write(" , , , , " + appPerm + "\n");
        csvFinisher(csvWriter, appFiles, iCCList, manifest);        
        csvWriter.close();
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

    public static void csvFinisher(FileWriter csvWriter, File[] appFiles, ArrayList<iCComp> iCCList, File manifest) throws IOException{
        for (File file : appFiles){
            if (file.isDirectory() && file.getAbsolutePath().contains("smali")){
                File[] subDirectory = file.listFiles();
                csvFinisher(csvWriter, subDirectory, iCCList, manifest);
            }
            if (file.isFile()){
                iCCList = iccFinder(file, manifest);
                for (iCComp i: iCCList){
                    csvWriter.append(i.toCSVFormat());
                }
            }
        }
    }

    public static ArrayList<iCComp> iccFinder(File file, File manifest) throws FileNotFoundException{

            ArrayList<iCComp> iCCList = new ArrayList<iCComp>();
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
                for(String i : methods){
                    if(current.contains(i) && !current.contains(".method")){
                        iCCList.add(new iCComp(i));
                        iCCList.get(count).setSourceComp(currentSource[0]);
                        iCCList.get(count).setTargetComp(currentTarget[0]);
                        iCCList.get(count).setTypeComm(findTypeComm(manifest, iCCList.get(count).getSourceComp()));
                        count++;
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
                while(!current.contains("</intent-filter>") && !current.contains("</manifest>")){
                    current = scanner.nextLine();
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




> firstLaunchDate
 
 ClassName : AppsFlyerLib.java
 
 LineNumber: 2491
 
 key code:
 ```
     private String getFirstInstallDate(SimpleDateFormat dateFormat,Context context) {
         SharedPreferences sharedPreferences = getSharedPreferences(context);
         String firstLaunchDate = sharedPreferences.getString(FIRST_INSTALL_PREF, null);
         if (firstLaunchDate == null) {
             if (isAppsFlyerFirstLaunch(context)){
                 AFLogger.afDebugLog("AppsFlyer: first launch detected");
                 firstLaunchDate = dateFormat.format(new Date());
             } else {
                 firstLaunchDate = ""; // unknown
             }
             saveDataToSharedPreferences(context, FIRST_INSTALL_PREF, firstLaunchDate);
         }
 
         AFLogger.afInfoLog("AppsFlyer: first launch date: "+firstLaunchDate);
 
         return firstLaunchDate;
     }
 ```
  
----



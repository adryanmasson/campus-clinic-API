# MANUAL STEP REQUIRED - Update Azure App Service Environment Variable

## You need to update the database connection string in Azure Portal:

1. Go to Azure Portal: https://portal.azure.com
2. Navigate to your App Service: **clinica-api-adryan**
3. Go to **Configuration** → **Application settings**
4. Find the setting: **SPRING_DATASOURCE_URL**
5. Change the value from:
   ```
   jdbc:sqlserver://clinica-campus-banco.database.windows.net:1433;database=clinica_do_campus;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
   ```
   
   To:
   ```
   jdbc:sqlserver://clinica-campus-banco.database.windows.net:1433;database=campus_clinic;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
   ```

6. Click **Save**
7. Click **Continue** to restart the app
8. Wait about 2 minutes for the deployment to complete

## After updating, the API will automatically redeploy with:
- ✅ All code in English
- ✅ New English database: campus_clinic
- ✅ New English endpoints: /api/specialties, /api/doctors, /api/patients, /api/appointments, /api/medical-records
- ✅ English sample data

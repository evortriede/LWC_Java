# LWC_Java
 Java programs for London Water Co-op

## Introduction

London Water Cooperative (LWC) operates a small water treatment plant (WTP) where a number of metrics are collected and transmitted to a remote location where they are recorded. The metrics include:

- How much water is in the storage tanks
- When the pressure pump supplying the users turns on (and how long since it last ran)
- When the pressure pump turns off (and how long it ran)
- That the water usage has exceeded a threshold
- And when the WTP is running (i.e., when it is actively filling the storage tanks - it runs in batches)
  - The chlorine concentration in the water going into the tanks
  - The turbidity of the water going into the tanks
  - The speed at which the chlorine pump is running
  
Since there is no Internet or cellular service at the WTP location, these metrics are collected by microcontrollers and transmitted to the remote location using LoRa (a long-range radio technology). The remote location has a computer that records the metrics using one of the Java programs in this repository (`com.lwc.CurrentRecorder`).

It is important that the CurrentRecorder program runs at all times and restarts when the computer hosting it is restarted (think power outage). To accomplish this the CurrentRecorder program is run as a Windows service by utilizing an open-source project called WinRun4J. It uses `com.lwc.CurrentMonitorService` to front end `com.lwc.CurrentRecorder`.

The Oregon Health Authority (OHA) requires monthly reporting from all water systems of the turbidity and volume metrics. This repository contains the Java programs (`com.lwc.VolTurbProcessor` and `com.lwc.LWCMonthlyReport`) that are used to process the recorded metrics into the required reports. (See Creating OHA Reports below.)

### Update - WTP now has Internet

A radio link has been installed between the remote location and the WTP, extending the remote location's network to the WTP. As of this writing, none of the infrastructure described herein has changed except for the addition of a gateway service for Modbus traffic that runs on the PC that is at the WTP. Before the new network connection was established, the PLC was plugged directly into the WiFi router's WAN port. As such, the computers in the WTP could talk directly to the PLC through WiFi. Now however, the new network connection plugs into the WAN port on the router and the PLC is connected to the PC's Ethernet port. So, for the microcontrollers in the WTP to talk to the PLC, they have to go through the PC. The gateway service (`com.lwc.ModbusGatewayService`) listens for Modbus traffic (port 502) and forwards it to the PLC.

Eventually, we will take advantage of the new connectivity. For now, it is nice to be able to use WiFi calling, etc. from the WTP.

## Installation

Hopefully, these instructions will never have to be used as there is already a remote computer with all of this software installed, configured and running. If, however, something should happen to the remote computer and it has to be re-built and the backups have somehow disappeared or become corrupt, these instructions can be used to get things going again.

Here are the steps:

1. Install the latest Java JDK
2. Install GitHub Desktop
3. Install Microsoft Excel
4. Clone this repository to the new computer
5. Open a CMD or Power Shell window in administrator mode
6. CD to the `bin` directory of the cloned repository
7. Run the `build.bat` script (compiles the Java programs and creates `lwc.jar`)
8. Run the `configure.bat` script (creates `CurrentRecorderService.ini` - look it over as a sanity check)
9. Run the `registersvc.bat` script (Installs the program as a Windows service)
10. Check `Windows Services` to make sure that the service is running and set to automatically start.

## Theory of Operation

The microcontroller that the Current Monitor Service connects to (the Current Recorder) sends metrics that it receives from a microcontroller at the WTP (the Current Monitor). These metrics are recorded by the Current Monitor Service to two files: one containing the raw data and another containing interpreted metrics. These files are rotated each month and live in the services directory of the clone of this repository on the computer running the service. The raw data file is named `m-yyyy-raw.txt` and the processed data file is named `m-yyyy-cooked.txt` where `m` is the one- or two-digit month and `yyyy` is the four-digit year for the date corresponding to the data.

The `report.bat` script runs a Java program (`com.lwc.VolTurbProcessor`) that reads `m-yyyy-cooked.txt` and produces `m-yyyy-fixed.txt` and then runs another program (`com.lwc.LWCMonthlyReport`) which reads the "fixed" file and writes the two .csv files, `MMsheet1.csv` and `MMsheet2.csv` which are used to create the Excel workbook that OHA needs.

VolTurbProcessor makes two passes through the "cooked" file. The first pass is to locate the start and end of WTP runs within the data. The second pass looks for missing and spurious data and writes the "fixed" file. The missing data is manufactured and the spurious data is removed. Also, markers are placed at the start and end of WTP runs as identified in the first pass.

LWCMonthlyReport reads the "fixed" file and computes the data for the cells in the Excel workbook, writing them the .csv files. Four-hour turbidity numbers are averages over each four hour period and the maximum number is the highest single sample in a day. The numbers for amount of water produced are computed from tank level readings while hourly usage is computed from pressure pump invocations.

That's the high-level view. For more detail, look at the code.

## Creating OHA Reports

Creating of the OHA Turbidity report is a semi-automatic process. The report is an Excel spread sheet with two pages (a third page is instructions). There are a number of conditions that can affect the raw data that is collected by the CurrentRecorder program that can cause errors in the report generation, so it is important that the finished product be scrutinized for sanity and corrected if required. When there are errors, it is best to correct the source data if possible and re-generate the report.

Here are some of the data issues that can cause errors in the report:

- Missing data. This can happen for a variety of reasons, but is only of concern if it happens while the WTP is running. The VolTurbProcessor program attempts to fill in missing data while the WTP is running, but it doesn't always have the desired effect.
- Spurious values. Turbidity and tank volume data are read from the PLC at the WTP. The VolTurbProcessor program determines that the WTP is running by looking at the tank volume over time. When the tank volume increases by 50 gallons or more over the course of a minute, it assumes that the plant is running. Now, from time to time the PLC will report a value for tank volume that is significantly different from reality. In these instances, the VolTurbProcessor can be fooled that the plant has started running or that it has stopped when it is still running. Similar data glitches can happen with turbidity metrics. This is usually evident when the four-hour turbidity numbers in the report look reasonable but the "Highest for the Day" number is very high. 
- Plant maintenance and/or interrupted runs. All sorts of interesting data can be generated during WTP maintenance.

### Instructions

1. Run the `reports.bat` script in the `bin` directory of this repository. With no parameters, it will create the artifacts required for the previous month. To run the script for a specific month, enter the two-digit month and four-digit year. For example, `.\report.bat 01 2023`.

![Run Report Script](/assets/RunReportScript.png)

2. In the `reports` directory of this repository, open `blank.xlsx` in Excel

![Open Blank](/assets/OpenBlank.png)

3. Use File->Save As to save `blank.xlsx` with a new name. For example, `Jan2023.xlsx`.

![Save As](/assets/SaveAs.png)

4. Change the date in cell L2 of the `Turbidities & Flow` sheet.

![Change Date](/assets/ChangeDate.png)

5. The `report.bat` script will have created two .csv files in the `reports` directory named `MMsheet1.csv` and `MMsheet2.csv` where `MM` is the two-digit month number corresponding to the month for reporting is being done. For example, `01sheet1.csv` and `01sheet2.csv`. Open `MMSheet1.csv` with Excel, then select and copy cells A1 to K31. (Note: select all of these cells even if there is no data in some of the cells.)

![Copy Sheet 1](/assets/Copy_Sheet1.png)

6. Switch to the report workbook and right click cell C5. Select the `fx` (i.e., Formulas) Paste Option.

![Paste Sheet 1](/assets/PasteSheet1.png)

7. Open `MMsheet2.csv` then select and copy cells A1 to E31

![Copy Sheet 2](/assets/CopySheet2.png)

8. Switch to the report workbook and right click on cell B10 of the `UV CTs` sheet. Select the `fx` Paste Option.

![Paste Sheet 2](/assets/PasteSheet2.png)

9. Save the worksheet. It is now ready to be printed, signed and mailed.

As noted before, the details in the workbook should be scrutinized for sanity before mailing. For example, if the maximum turbidity for a given day is much higher than any of the four-hour values or if there are any values recorded for days when the WTP was not running. These anomalies should be located in the raw data, corrected and the whole process repeated. Alternatively, the Excel workbook can be manually corrected if the proper values are known.

When making corrections to the raw data, it is usually easiest to modify the "fixed" data file because it has the markers for the start and end of WTP runs as well as all of the generated data which are the most common culprits needing correction. When corrections are made to the "fixed" file, the .csv files can be generated by running the `report.bat` script with the date and a third parameter as follows: `.\report.bat mm yyyy x` where mm and yyyy are the month and year and `x` is literally 'x' (or any other character; it is simply a flag indicating that the "fixed" file is to be processed without first processing the "cooked" file). Note that if you make changes the "fixed" file and run `report.bat` without all three parameters, the "fixed" file will be overwritten and your modifications will be lost, so be careful!

## Modbus Gateway Service

The Modbus Gateway Service runs on the WTP PC, forwarding Modbus traffic received over WiFi to the PLC. It is a Java program that runs as a Windows Service by virtue of the WinRun4J package.

### Installation

Hopefully, these instructions will never have to be used as the PC at the WTP already has this software installed, configured and running. If, however, something should happen to the WTP PC and it has to be re-built and the backups have somehow disappeared or become corrupt, these instructions can be used to get things going again.

Here are the steps:

1. Install the latest Java JDK
2. Install GitHub Desktop
3. Clone this repository to the new computer
4. Open a CMD or Power Shell window in administrator mode
5. CD to the `bin` directory of the cloned repository
6. Run the `build.bat` script (compiles the Java programs and creates `lwc.jar`)
7. Run the `configure_gateway_svc.bat` script (creates `ModbusGatewayService.ini` - look it over as a sanity check)
8. Run the `register_gateway_svc.bat` script (Installs the program as a Windows service)
9. Check `Windows Services` to make sure that the service is running and set to automatically start.

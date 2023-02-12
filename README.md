# LWC_Java
 Java programs for London Water Co-op

## Introduction

London Water Cooperative (LWC) operates a small water treatment plant (WTP) where a number of metrics are collected and transmitted to a remote location where they are recorded. The metrics include:

- How much water is in the storage tanks
- When the pressure pump supplying the users turns on (and how long since it last ran)
- When the pressure pump turns off (and how long it ran)
- That the water usage has exceeded a threshold
- And when the WTP is running (i.e., when it is activly filling the storage tanks - it runs in batches)
  - The chlorine concentration in the water going into the tanks
  - The turbidity of the water going into the tanks
  - The speed at which the chlorine pump is running
  
Since there is no Interneet or cellular service at the WTP location, these metrics are collected by microcontrollers and transmitted to the remote location using LoRa (a long range radio technology). The remote location has a computer that records the metrics using one of the Java programs in this repository (com.lwc.CurrentRecorder).

It is important that the CurrentRecorder program runs at all times and restarts when the computer hosting it is restarted (think power outage). To accomplish this the CurrentRecorder program is run as a Windows service by utilizing an open source project called WinRun4J. It uses com.lwc.CurrentRecorderService to front end com.lwc.CurrentRecorder.

The Oregon Health Authority (OHA) requires monthly reporting from all water systems of the turbidity and volume metrics. This repository contains the Java programs (com.lwc.VolTurbProcessor and com.lwc.LWCMonthlyReport) that are used to process the recorded metrics into the required reports. (See Creating OHA Reports below.)

## Installation

Hopefully, these instructions will never have to be used as there is already a remote computer with all of this software installed, configured and running. If, however, something should happen to the remote computer and it has to be re-built and the backups have somehow disappeared or become corrupt, these instructions can be used to get things going again.

Here are the steps:

1. Install the latest Java JDK
2. Install GitHub Desktop
3. Install Microsoft Excel
4. Clone this repository to the new computer
5. Open a CMD or Power Shell window in administrator mode
6. CD to the bin directory of the cloned repository
7. Run the build.bat script (compiles the Java programs and creates lwc.jar)
8. Run the configure.bat script (creates CurrentRecorderService.ini - look it over as a sanity check)
9. Run the registersvc.bat script (Installs the program as a Windows service)
10. Check Windows Services to make sure that the service is running and set to automatically start.

## Creating OHA Reports

Creating of the OHA Turbidity report is a semi-automatic process. The report is an Excel spread sheet with two pages (a third page is instructions). There are a number of conditions that can affect the raw data that is collected by the CurrentRecorder program that can cause errors in the report generation, so it is important that the finished product be scrutenized for sanity and corrected if required. When there are errors, it is best to correct the source data if possible and re-generate the report.

Here are some of the data issues that can cause errors in the report:

- Missing data. This can happen for a variety of reasons, but is only of concern if it happens while the WTP is running. The VolTurbProcessor program attempts to fill in missing data while the WTP is running, but it doesn't always have the desired effect.
- Spurious values. Turbidity and tank volume data are read from the PLC at the WTP. The VolTurbProcessor program determines that the WTP is running by looking at the tank volume over time. When the tank volume increases by 50 gallons or more over the course of a minute, it assumes that the plant is running. Now, from time to time the PLC will report a value for tank volume that is significantly different from reality. In these instances, the VolTurbProcessor can be fooled that the plant has started running or that it has stopped when it is still running. Similar data glitches can happen with turbidity metrics. This is usually evident when the four hour turbidity numbers in the report look reasonable but the "Highest for the Day" number is very high. 
- Plant mantanence and/or interrupted runs. All sorts of interesting data can be generated during WTP mantanence.

### Instructions

1. Run the reports.bat script in the bin directory of this repository. With no parameters, it will create the artifacts required for the previous month. To run the script for a specific month, enter the two digit month and four digit year. For example `.\report.bat 01 2023`.

![Run Report Script](/assets/RunReportScript.png)

2. In the reports directory of this repository, open blank.xlsx in Excel

![Open Blank](/assets/OpenBlank.png)

3. Use File->Save As to save blank.xlsx with a new name. For example `Jan2023.xlsx`.

![Save As](/assets/SaveAs.png)

4. Change the date in cell L2 of the Turbidities & Flow sheet.

![Change Date](/assets/ChangeDate.png)

5. The report.bat script will have created two .csv files in the reports directory named MMsheet1.csv and MMsheet2.csv where MM is the two digit month number corresponding to the month for reporting is being done. For example 01sheet1.csv and 01sheet2.csv. Open MMSheet1.csv with Excel, then select and copy cells A1 to K31.

![Copy Sheet 1](/assets/CopySheet1.png)

6. Switch to the report workbook and right click cell C5. Select the `fx` Paste Option.

![Paste Sheet 1](/assets/PasteSheet1.png)

7. Open MMsheet2.csv then select and copy cells A1 to E31

![Copy Sheet 2](/assets/CopySheet2.png)

8. Switch to the report workbook and right click on cell B10. Select the `fx` Paste Option.

![Paste Sheet 2](/assets/PasteSheet2.png)

9. Save the worksheet. It is now ready to be printed, signed and mailed.

As noted before, the details in the workbook should be scrutenized for sanity before mailing. For example, if the maximum turbidity for a given day is much higher than any of the four hour values or if there are any values recorded for days when the WTP was not running. These anomolies should be located in the raw data, corrected and the whole process repeated. Alternativly, the Excel workbook can be manually corrected if the proper values are known.

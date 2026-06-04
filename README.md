# MoneyGoat – Personal Budgeting App
Technical Report & Project Documentation

## 1. Executive Summary
MoneyGoat is a robust Android application designed to empower users with comprehensive personal finance management tools. By integrating structured expense tracking, categorical analysis, and goal-oriented budgeting, the application provides a holistic view of a user's financial health. The primary objective of MoneyGoat is to transform passive expense recording into active financial engagement.

## 2. Core Feature Set
The application offers a specialized suite of features tailored for modern financial tracking:
* **Secure Authentication**: Personal user accounts with persistent session management via Room and SharedPreferences.
* **Dynamic Category Management**: User-defined spending categories for personalized organization.
* **Granular Expense Tracking**: Detailed transaction logging including timestamps, descriptions, and amounts.
* **Spending Analytics**: Categorical data aggregation and visual representation using interactive graphs.
* **Goal-Based Budgeting**: Monthly threshold setting (Minimum/Maximum) to regulate cash flow with real-time visual feedback.
* **Online Database**: Automated synchronization of all local data to Firebase Realtime Database for secure cloud storage.

## 3. Special Features (POE Requirements)

### Feature 1: Digital Receipt Archiving (Own Feature 1)
To bridge the gap between digital tracking and physical proof, MoneyGoat includes an integrated camera module.
* **Implementation**: Users can tap "Take Photo" when adding an expense. The app launches the system camera, captures the receipt, stores it in the app's private directory, and links the image URI to the specific database record.
* **Benefit**: Ensures data integrity and provides evidence for tax or reimbursement purposes.

### Feature 2: Temporal Filtering & Advanced Search (Own Feature 2)
MoneyGoat provides a powerful filtering engine to allow users to audit their financial history effectively.
* **Implementation**: In both the "Expense History" and "Spending Analytics" screens, users can select custom start and end dates. The app performs real-time SQL queries to filter records within that specific temporal window.
* **Benefit**: Allows for precise tracking of spending over weeks, months, or custom holiday periods.

### Feature 3: Visual Analytics & Goal Comparisons
For the final submission, we have implemented advanced data visualization using the MPAndroidChart library.
* **Categorical Graph**: An interactive Pie Chart that displays the percentage distribution of spending across all categories for any selected period.
* **Goal Progress Graph**: A comparative Bar Chart that displays "Actual Spent" alongside the user's "Minimum Savings Goal" and "Maximum Spending Limit," providing immediate insight into financial discipline.

## 4. Functional Architecture

### 4.1 Authentication and Security
The entry point utilizes a login/registration system. User credentials are encrypted and managed via Room, while session persistence ensures a seamless user experience.

### 4.2 Firebase Cloud Sync
Data is stored locally in Room for offline availability and automatically synced to **Firebase Realtime Database**. This ensures that even if the device is lost, the user's financial data remains accessible online.

### 4.3 Data Visualization
The Analytics engine uses MPAndroidChart to transform raw SQL aggregations into actionable insights, highlighting potential areas for cost-saving.

## 5. Technical Specifications
* **Language**: Kotlin (JVM 17)
* **Minimum SDK**: API 26 (Android 8.0)
* **Target SDK**: API 35 (Android 15)
* **Architecture**: MVVM (Model-View-ViewModel)
* **Database**: Room (Local) & Firebase Realtime Database (Online)
* **UI Framework**: Material Design 3, View Binding, RecyclerView
* **Charts**: MPAndroidChart
* **Concurrency**: Kotlin Coroutines & LiveData

## 6. Conclusion
MoneyGoat serves as a comprehensive solution for users seeking to improve their financial literacy. By combining ease of use with detailed data entry and analytical insights, the application bridges the gap between simple ledger keeping and proactive wealth management.

# MyGuideFirebase

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Project Overview

**MyGuideFirebase** is an Android application designed to connect tourists with local guides. Tourists can browse profiles of certified and uncertified guides, book tours, and manage their bookings directly from the app. Guides can offer various services, set their availability, and manage bookings. The app uses Firebase for authentication, real-time database management, and cloud storage.

## Features

- **User Authentication**: Secure login and registration with Firebase Authentication.
- **Guide Profiles**: Detailed profiles for guides, including services offered, pricing, and availability.
- **Tourist Profiles**: Tourists can manage their preferences, bookings, and browse guides.
- **Booking System**: Tourists can book guides based on availability and manage their bookings.
- **Notifications**: Push notifications for booking confirmations and updates.
- **Multi-language Support**: Users can select and view content in multiple languages.

## Technologies Used

- **Programming Language**: Java
- **Android Architecture Components**: LiveData, ViewModel
- **Firebase Services**:
  - Firebase Authentication
  - Firebase Firestore (Database)
  - Firebase Storage
  - Firebase Cloud Messaging (FCM) for notifications
- **UI/UX**: Material Design, Glide for image loading
- **Tools**: Android Studio, Git, GitHub

## Installation

### Prerequisites

- **Android Studio**: Ensure you have the latest version installed.
- **Firebase Project**: Create a Firebase project and add your Android app to it.

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/username/MyGuideFirebase.git
Open in Android Studio:

Open Android Studio and select "Open an existing project."
Navigate to the cloned repository and open it.
Configure Firebase:

Download the google-services.json file from your Firebase Console and place it in the app/ directory.
Ensure that your Firebase configuration in build.gradle is correct.
Build the Project:

Allow Android Studio to sync and build the project.
Resolve any dependencies or issues that may arise.
Run the Application:

Connect an Android device or start an emulator.
Click the "Run" button in Android Studio.
Usage
Register or Log In
Register: New users can register with their email or existing accounts.
Log In: Existing users can log in using their credentials.
Set Up Profile
After logging in, set up your profile based on whether you are a tourist or a guide.
Browse Guides
Tourists can search for guides by location, language, and services offered.
Book a Guide
Select available dates and book a guide directly through the app.
Manage your bookings under the "My Bookings" section.
Receive Notifications
Get notified of booking confirmations and updates.
Contributing
Contributions are welcome! To contribute:

Fork the Repository: Click the "Fork" button at the top right of the repository page.

Clone Your Fork:

bash
Copy code
git clone https://github.com/your-username/MyGuideFirebase.git
Create a New Branch:

bash
Copy code
git checkout -b feature/YourFeatureName
Make Your Changes: Commit your changes with clear commit messages.

Push to Your Fork:

bash
Copy code
git push origin feature/YourFeatureName
Submit a Pull Request: Go to the original repository and create a pull request from your fork.

License
This project is licensed under the MIT License. See the LICENSE file for details.

Contact
For any inquiries, please contact:

Name: Your Name
Email: your.email@example.com
GitHub: username

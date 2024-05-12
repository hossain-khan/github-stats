Project Setup
=============

Setup Walkthrough
------------------

[![](https://user-images.githubusercontent.com/99822/203117567-4742aafc-d0ae-4586-bef7-0d539c714fad.jpg)](https://www.youtube.com/watch?v=CftaVHzsX3A)


Requirements
------------
* IntelliJ Idea Community Edition ([Download](https://www.jetbrains.com/idea/download/))
* Java SDK (JDK)
   * If JDK is missing, don't worry, IntelliJ will assist you installing JDK from the IDE. See [troubleshooting](#troubleshooting).

Steps
-------

### Clone Repo
Clone repo with your favorite tool, or with CLI using following command
```
git clone https://github.com/hossain-khan/github-stats.git
```

<img width="250" src="https://user-images.githubusercontent.com/99822/197037727-bb55fa9e-c1d8-4ede-ae53-ae21c5fbe362.png"/>


### Open in IntelliJ
Open the checked out directory in IntelliJ IDEA 

> ℹ️ Instead of using IntelliJ, you can use Gradle command after configuring [`local.properties`](#setup-localproperties)
> `./gradlew run`

<img width="700" alt="IntelliJ open" src="https://user-images.githubusercontent.com/99822/197038306-3f3dfd92-54d5-45fa-971b-70203b673273.png">
<img width="400" alt="Select dir" src="https://user-images.githubusercontent.com/99822/197038309-299f6ab3-b2fd-477c-8c61-542525f7a726.png">

Once project is synced using gradle, you should see the project load like following

<img width="800" src="https://user-images.githubusercontent.com/99822/197039976-0fa4e15f-e3c1-4dbc-892f-572e15da3a6a.png">

### Setup `local.properties`
Rename the provided [`local_sample.properties`](https://github.com/hossain-khan/github-stats/blob/main/local_sample.properties) to `local.properties`.

<img width="800" src="https://user-images.githubusercontent.com/99822/197039882-33108a1f-64d0-4848-bfbb-de3555f449b0.png">
<img width="350" src="https://user-images.githubusercontent.com/99822/197039879-e092e9bc-305e-485b-b1de-200e8c5418f3.png">

Generate **[token](https://github.com/settings/tokens)** and update other configurations as appropriate

### Generate stats using `Main.kt`
Once configuration is in place, all you need to do is open the [`Main.kt`](https://github.com/hossain-khan/github-stats/blob/main/src/main/kotlin/Main.kt#L10) and ▶️ run it.

<img width="307" src="https://user-images.githubusercontent.com/99822/197039854-641344eb-5e75-472c-a911-331923f6b163.png">
<img width="360" src="https://user-images.githubusercontent.com/99822/197039857-dda04738-6b24-4389-a87c-95816225e513.png">

#### Run using gradle command

You can also use following gradle command to run the stats generator
```
./gradlew run
```

> See troubleshooting section if you encounter issue.

### Preview Stats
Once the generator app finishes running, you will see report in the project root directory that contains both `ASCII` and `CSV` report.  
Here is sample snapshot of reports from different repositories:

<img width="400" alt="Generated files and folders" src="https://user-images.githubusercontent.com/99822/198861052-a4362440-d09c-4e06-aac0-e344b519299c.png">

Troubleshooting
---------------

If you see following error, it could be your token is not setup, or you have exceeded API rate limit.
```
Exception in thread "main" retrofit2.HttpException: HTTP 401 
```

Open [BuildConfig](https://github.com/hossain-khan/github-stats/blob/main/src/main/kotlin/dev/hossain/githubstats/BuildConfig.kt#L23) and enable `DEBUG_HTTP_REQUESTS` to `true` to see API request header information to confirm error details.

--- 

Missing ▶️ run button, or JDK missing - in this case, take a look at top right corner of the IntelliJ IDEA that will suggest you to install a JDK. 
Click on the install link and wait for it to complete.

Finally, click on the gradle sync button (🐘🔁) and wait for the sync to complete. If everything went well you should see the ▶️ icon beside `main()` in the `Main.kt` file.

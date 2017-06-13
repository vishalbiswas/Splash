# Splash
Splash is a fast and lightweight forum frontend framework for Android. It supports
markdown and is similar to how reddit works. This project is not a standalone application.
It is meant to be deployed in conjunction with two other repositories: [Splash Android Frontend](https://www.github.com/vishalbiswas/splash)
and [Splash backend database](https://www.github.com/vishalbiswas/splash-backend-database)

## Requirements
* Android 4.1 and above, API Level 16

Splash is meant to be modular in the sense that you can replace the webservice with
your own implementation and mostly it depends on you doing just that. To create your
own implementation, you need to follow these guidlines.

## Guidelines for webservice
All the pages should be developed in accordance to REST API.
### Request type: POST
1.  http://hostname/**comment**/threadid
Description	: Reply to a thread
Input		: threadid, content, author
Output		: commentid, ctime, mtime
Used In		: CommentActivity

2.	http://hostname/**register**
Description	: Create new account
Input		: user, email, pass
Output		: status (code)
Used In		: RegisterActivity

3.	http://hostname/**update**/uid
Description	: Modify user data
Input		: uid, fname, lname
Optional Input	: email, password, profpic (attachment id)
Output		: status (code), fname, lname
Optional Output: email, profpic (attachment id)
Used In		: ProfileActivity

4.	http://hostname/**login**
Description	: Login
Input		: user, pass
Output		: status (code), uid, user, email
Optional Output: fname, lname, profpic
Used In		: LoginActivity

5.	http://hostname/**editpost**/threadid
Description	: Edit already existing post
Input		: threadid, title, content, topicid, attached
Output		: mtime
Used In		: PostActivity

6.	http://hostname/**post**
Description	: Create a new post
Input		: title, content, author, topicid, attached
Output		: threadid, ctime, mtime
Used In		: PostActivity

7.	 http://hostname/**upload**
Description	: Upload an attachment
Input		: attach (file)
Output		: status, attached
Used In		: SplashCache

### Request Method: GET
1.	http://hostname/**comments**/threadid
Description	: Retrieve comments for specific thread
Input		: threaded
Output		: List<Comment>
Used In		: ViewThreadActivity

2.	http://hostname/**search**/query
Description	: Search threads with "query" in title or content
Input		: query
Output		: List<Thread>
Used In		: NewsFeed

3.	http://hostname/**threads**/quantity
Description	: Retrieve "quantity" newest threads
Input		: quantity
Output		: List<Thread>
Used In		: NewsFeed

4.	http://hostname/**check**/username
http://hostname/check/email
Description	: Check if user data already exists
Input		: username or email
Output		: boolean key available
Used In		: FieldValidator

5.	http://hostname/**getuser**/uid
Description	: Retrieve user data
Input		: uid
Output		: username, uid, email
Optional Output: fname, lname, profpic (attachment id)
Used In		: SplashCache

6.	http://hostname/**attachment**/attachid
Description	: Retrieve attachment
Input		: attached
Output	: Does NOT output JSON. Instead, outputs raw   binary content
Used In	: SplashCache

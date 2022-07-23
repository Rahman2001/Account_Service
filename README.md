# Account_Service

This project focuses on Spring Security in Spring Boot. The goal of this project is to show API restrictions to various users and their functionality.
</br></br>

## APIs of application and permissions for users by their roles.
Below are APIs with their restrictions according to user's role. (Example, if a user is "ADMINISTRATOR", then he can can access certain APIs as below table shows).</br></br>
| | Anonymous | User | Accountant | Administrator |
| --- | --- | --- | --- | --- |
| POST api/auth/signup | + | + | + | + |
| POST api/auth/changepass | | + | + | + |
| GET api/empl/payment | - | + | + | - |
| POST api/acct/payments | - | - | + | - |
| PUT api/acct/payments | - | - | + | - |
| GET api/admin/user | - | - | - | + |
| DELETE api/admin/user/{email} | - | - | - | + |
| PUT api/admin/user/role | - | - | - | + |

</br> </br>
## Features of APIs.
1. POST <code>api/auth/signup</code> for registration of a user.
2. POST <code>api/auth/changepass</code> for updating password
3. GET <code>api/empl/payment</code> to view payments of employees (if there is any).
4. POST <code>api/acct/payments</code> to upload payment of employee (for particular period if there is no such).
5. PUT <code>api/acct/payments</code> to change a salary of employee in particular period (if there is such period).
6. GET <code>api/admin/user</code> to view all users in database with their role. (e.g. "ROLE_USER", "ROLE_ADMINISTRATOR", etc.)
7. DELETE <code>api/admin/user/{email}</code> to delete a user from database.
8. PUT <code>api/admin/user/role</code> to grant or remove any role from user.
</br>

## Examples of sending correct JSON data to APIs.
1. POST <code>api/auth/signup</code> : </br>
   ~~~json
   {
     "name":"Rahman",
     "lastname":"Rejepov",
     "email":"rahman@acme.com",
     "password":"rahmanGaziUniversity"
   }
   ~~~
   In example above, sent JSON data will be processed for verification. </br>
       i. If **email** has already been registered before, it will throw exception. </br>
       ii. If **password** does not support all the conditions in PasswordAuthentication.java, it will throw exception.</br></br>

2. POST <code>api/auth/changepass</code> : </br>
   ~~~json
   {
      "new_password": "your new Password" (for updating your password, you must be authenticated, first)
   }
   ~~~
</br>

3. GET <code>api/empl/payment</code>: </br>
    *GET request for api/empl/payment with the correct authentication for johndoe@acme.com* :</br> In case of successful authentication, application will return all the payrolls of that employee: </br>
   ~~~json
       [
          {
             "name": "John",
             "lastname": "Doe",
             "period": "March-2021",
             "salary": "1234 dollar(s) 56 cent(s)"
          },
          {
             "name": "John",
             "lastname": "Doe",
             "period": "February-2021",
             "salary": "1234 dollar(s) 56 cent(s)"
          },
          {
             "name": "John",
             "lastname": "Doe",
             "period": "January-2021",
             "salary": "1234 dollar(s) 56 cent(s)"
          }
      ]
   ~~~
   </br>
4. POST <code>api/acct/payments</code>: </br>
If a user wants to upload employee payrolls, then JSON data should be like this:</br>
   ~~~json
   [
       {
           "employee": "johndoe@acme.com",
           "period": "01-2021",
           "salary": 123456
       },
       {
           "employee": "johndoe@acme.com",
           "period": "01-2021",
           "salary": 123456
       }
   ]
   ~~~
</br>
  
5. PUT <code>api/acct/payments</code>: </br>If there is an employee with uploaded payment such as: </br>
   ~~~json
   {
    "employee": "johndoe@acme.com",
    "period": "01-2021", (period existing in database)
    "salary": 123457  (updated salary for that period)
   }
   ~~~
   Then, in case of successful update, the response will be like: </br>
   ~~~json
   {
   "status": "Updated successfully!"
   }
   ~~~
   </br>
6.  GET <code>api/admin/user</code>: </br>Shows all the users in database with their granted roles (for example): </br>
    ~~~json
    [
       {
           "id": 1,
           "name": "John",
           "lastname": "Doe",
           "email": "johndoe@acme.com",
           "roles": [
               "ROLE_ADMINISTRATOR"
           ]
       },
       {
           "id": 2,
           "name": "Ivan",
           "lastname": "Ivanov",
           "email": "ivanivanov@acme.com",
           "roles": [
               "ROLE_ACCOUNTANT",
               "ROLE_USER"
           ]
       }
    ]
    ~~~
     In example above, in order to get user list, user must be authenticated and authorized as **ADMINISTRATOR**.</br>
     
 7. DELETE <code>api/admin/user/{email}</code>: </br>In **{email}** , there must be existing user email to be able to delete that user as **ADMINISTRATOR** : </br>
    Example: Requested DELETE <code>api/admin/user/johndoe@acme.com</code> to delete a user with email  *johndoe@acme.com* :</br>
    Response: (in case of successful deletion):</br>
    ~~~json
    {
    "user": "ivanivanov@acme.com",
    "status": "Deleted successfully!"
    }
    ~~~
    </br>
 
 8. PUT <code>api/admin/user/role</code>: </br> In case of successful authentication and authorization as **ADMINISTRATOR** of a user, a user can **grant** or **remove** role of requested user: </br>
    ~~~json
     {
     "user": "ivanivanov@acme.com",
     "role": "ACCOUNTANT",
     "operation": "REMOVE"
     }
    ~~~
    In case of successful romoval of a role from the user, the response will be like:</br>
     ~~~json
      {
         "id": 2,
         "name": "Ivan",
         "lastname": "Ivanov",
         "email": "ivanivanov@acme.com",
         "roles": [
             "ROLE_USER"
         ]
      }
     ~~~
     *The same process is applied for **grant** operation.*

# JAVA JVM Dumper

Tool for Oracle and IBM JVMs threads and memory dumping and analyzing.
![alt tag](https://raw.githubusercontent.com/elkoolt/jvm-dumper/master/img/interface.png)

## How to use it?
It should be used in another running Spring v.4 framework based application. 
Build project with *mvn clean install* and place it to your project /lib directory.
Also place dependency libs (some of them could exist in your project).

Default profile is *basicprofile*, which does not require DB.

*dbprofile* enables database interactions: thread dump could be stored into DB as Clob.
But this feature was implemented just for fun, *basicprofile* meets all the needs.

But if there is a need to enable this feature, it could be done as follows:
User creation on Oracle 12c DB USERS tablespace when *dbprofile* is used

```
create user JVMDUMPS identified by JVMDUMPS;
grant read, write on directory data_pump_dir TO JVMDUMPS;
grant connect,resource, create view to JVMDUMPS;
grant imp_full_database to JVMDUMPS;
grant create session to JVMDUMPS;
ALTER USER JVMDUMPS quota unlimited on USERS;
```
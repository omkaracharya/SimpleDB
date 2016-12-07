# SimpleDB
DBMS Project 2 - SimpleDB

Team number:- 35

i. Unity IDS:-

    Amit Watve          -   awatve
    Akshay Arlikatti    -   aarlika
    Omkar Acharya       -   oachary
    Nathan Faulkner     -   nafaulkn


ii. List of files changed:-

    Modified:
        LogIterator.java
        LogMgr.java
        BasicBufferMgr.java
        RecoveryMgr.java
        SetIntRecord.java
        SetStringRecord.java
        LogRecord.java
        LogRecordIterator.java
        BufferMgr.java
    
    
    Added: 
        ProjectTest.java
        TestRunner.java
        
iii. Instructions to run test:

    All tests are written using JUnit 4.
    
    1) Undo-Redo Test:
        Tests the undo redo functionality as described in the guidelines.
        Also implicitly tests forward iteration of logs while performing redo.
    2) Buffer Test:
        Tests the buffer manager functionality as described in the guidelines.
        Correctly implements FIFO replacement.
        Also implicitly tests bufferPoolMap.
        
        
    To run tests located in ProjectTest.java
    Simply run TestRunner.java (make sure JUnit 4.12 and hamcrest core 1.3 is on classpath)
    
    Example Execution:
        java -cp "~(path to junit jar)\junit\4.12\*;~(path to hamcrest jar)\hamcrest-core\1.3\*;..\;." TestRunner
    
    Example Output:
    
        new transaction: 1
        recovering existing database
        transaction 1 committed
        Undo Redo test passed.
        
        new transaction: 2
        recovering existing database
        transaction 2 committed
        Buffer FIFO test successful.
        
        true
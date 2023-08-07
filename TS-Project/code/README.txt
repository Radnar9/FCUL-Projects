<-> README <->

To execute the project you can either import the 3 applications to an IDE such as IntellijIDEA, or build and execute the applications with the following commands:

- Inside the MBeC directory:
$ gradlew build
$ java -jar ./build/libs/MBeC-all.jar <insert the desired arguments>

- Inside the store directory:
$ gradlew build
$ java -jar ./build/libs/store-all.jar <insert the desired arguments>

- Inside the bank directory:
$ gradlew build
$ java -jar ./build/libs/bank-all.jar <insert the desired arguments>

Note: -> If you are using MacOS/Linux use ./gradlew instead of gradlew.

Or even:
$ gradlew build
$ gradlew run --args="-u 1.user -a 1 -n 200.00"
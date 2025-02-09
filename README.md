# Prerequisitos

* [Mysql Server 8.0.4 y Mysql Workbench 8.0.4](https://dev.mysql.com/downloads/mysql/8.0.html)
  Ejecutar el instalador, seleccionar custom y seleccionar ambas opciones
  
  ![imagen](https://github.com/user-attachments/assets/822d0963-a51d-47fe-99ce-f0b8e8eeba63)
  
* [IntelliJ Idea Community](https://www.jetbrains.com/idea/download/?section=windows) (Descargar la versión Community, no Ultimate)
* [JDK-21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) y [Apache Maven 3.9.8](https://maven.apache.org/download.cgi): Tanto Java 21 como Maven deben de agregarse al [PATH](https://www.youtube.com/watch?v=km3tLti4TCM):

* [Launch4j](https://sourceforge.net/projects/launch4j/)


# Ejecución del programa

Se detallarán los pasos para ejecutar el programa dentro de IntelliJ IDEA, en caso de que se requieran realizar cambios de manera local.

* Importar datos de la base de datos:
  
  1. Se debe de crear una conexión dentro de Mysql Workbench
  2. Descargar el archivo [zip](https://github.com/Yi-Zhou-Zhou/HaisanPos/tree/master/dumps)
  3. Dentro de la conexión, seleccionar Server -> Data Import
  4. Generar un Schema (en caso de ser necesario) y seleccionarlo dentro de "Default Target Schema". Debería verse algo así
     ![imagen](https://github.com/user-attachments/assets/3721b1ca-f263-47c5-adcd-88a1b51f2180)
  5. Conectarse al Schema creado dandole doble click en la barra lateral izquierda (se puede corroborar que uno está conectado a un schema cuando éste está en negrita)
  6. Crear un usuario a través de la siguiente query:
     `INSERT INTO users (name, password) VALUES ('<USUARIO>', '<Contraseña>');`

* Archivo pom.xml: Se deben modificar los systemPath dentro del pom.xml, para que calce con las rutas de su ordenador:

   ```
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.4.0</version>
      <scope>system</scope>
      <systemPath>C:/Users/chonz/Documents/POS/lib/mysql-connector-j-8.4.0.jar</systemPath> ----> Modificar esto (línea 164)
    </dependency>
    <dependency>
        <groupId>Utils</groupId>
        <artifactId>util</artifactId>
        <version>1.0</version>
        <scope>system</scope>
        <systemPath>C:/Users/chonz/Documents/POS/lib/Util.jar</systemPath> ----> Modificar esto (línea 171)
    </dependency>
     ```

  * Agregar opciones de VM: Se debe de modificar la configuración de ejecución dentro de IntelliJ Idea:
    1. Dirigirse a Current File -> Edit Configurations
      ![imagen](https://github.com/user-attachments/assets/b10ac795-5dc5-47c9-8ca4-cc796173b3a2)

    2. Seleccionar el símbolo "+" y luego clickear "Application". Dentro de "Main class" se debe de agregar "com.example.poshaisan.Login". Además, se debe de corroborar que se está usando el SDK java-21.
    3. A la derecha de "Build and run" clickear "Modify options" y seleccionar "Add VM options", para agregar los siguientes exports
   ```
    --add-exports=javafx.graphics/com.sun.javafx.scene=org.controlsfx.controls
    --add-exports=javafx.graphics/com.sun.javafx.scene.traversal=org.controlsfx.controls
    --add-exports=javafx.graphics/com.sun.javafx.css=org.controlsfx.controls
    --add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls
    --add-exports=javafx.controls/com.sun.javafx.scene.control=org.controlsfx.controls
    --add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=org.controlsfx.controls
    --add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls
    --add-exports=javafx.base/com.sun.javafx.collections=org.controlsfx.controls
    --add-exports=javafx.base/com.sun.javafx.runtime=org.controlsfx.controls
    --add-exports=javafx.web/com.sun.webkit=org.controlsfx.controls
    --add-exports=javafx.graphics/com.sun.javafx.css=org.controlsfx.controls
    -Dnet.bytebuddy.experimental=true
    ```
    Se debe visualizar una pestaña así:
    ![imagen](https://github.com/user-attachments/assets/886d0996-3d98-47f2-b15f-2ecf4cb54497)

    Se deben de aplicar cambios y cerrar la pestaña. Al ejecutar el programa, debería correr sin problemas.

# Generación de programa ejecutable

## Generación de archivo JAR

  1. Dentro de IntelliJ, dirigirse a File -> Project Structure -> Artifacts. Seleccionamos el símbolo "+" -> Jar -> From modules with dependencies.
  2. Dentro de Main Class, seleccionamos el archivo LoginStarter.
  3. Aplicamos cambios y cerramos la pestaña.
  4. Dentro de Build, seleccionamos la opción "Build Artifacts" y "Build".
  5. Se generará la carpeta "out", el cual contendrá el proyecto exportado como JAR.

❗ **Cada vez que se realice algún cambio dentro del proyecto se debe de repetir el paso 4, para generar nuevamente el JAR**

 ## Generación de archivo .exe
 
  1. Dentro de Launch4j, se debe de rellenar el campo "Output file" con el directorio donde se quiere exportar el ejecutable y el nombre deseado **debe terminar en .exe**.
  2. En el campo "Icon", utilizar el .ico dentro de la carpeta resources/images/Icon.ico.
  3. Dentro de Single instance, marcar "Allow only a single...".
  4. En Mutex Name, colocar el mismo nombre ingresado en "Output file".
  5. Dentro de JRE, cerciorarse de que se esté usando el PATH: `%JAVA_HOME%;%PATH%` dentro de "JRE paths".
  6. Marcar la opción del engranaje e ingresar el mismo nombre ingresado en "Output file".


# Cosas a tener en consideración

* Archivo Utils.java: Dentro de Utils.java se encuentran las propiedades de cada restaurant, se recomienda cambiar: DISH_TYPES, SERVERS, DB_URL, DB_USER, DB_PASSWORD, RESTAURANT_NAME, RESTAURANT_ADDRESS, RESTAURANT_PHONE Y RESTAURANT_RUT.
* [Se recomienda colocar impresora térmica como predeterminada](https://www.youtube.com/watch?v=c5TFj5IYETk)

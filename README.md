# Grafika Komputerowa

## Development guide

- Download Java 21

> java --version
> - java 21.0.2 2024-01-16 LTS 
> - Java(TM) SE Runtime Environment (build 21.0.2+13-LTS-58)
> - Java HotSpot(TM) 64-Bit Server VM (build 21.0.2+13-LTS-58, mixed mode, sharing)

- Download JavaFX 21 SDK
> https://gluonhq.com/products/javafx/

- Paste javafx-sdk into main project folder


## IntelliJ configuration
- Set Java 21 as project sdk (File -> Project Structure -> Project Settings -> Project)

![Image1](/documentation/img.png)

- Add JavaFx SDK as a library (File -> Project Structure -> Project Settings -> Project)

![Image2](/documentation/img_1.png)

- Edit run configuration

Modify options -> Add VM options

Paste this configuration:

--module-path "javafx-sdk-21.0.4/lib" --add-modules javafx.controls,javafx.fxml

![Image3](/documentation/img_2.png)
![Image4](/documentation/img_3.png)





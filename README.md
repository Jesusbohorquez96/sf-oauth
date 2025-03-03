# SAMLAssertionGen - Guía de Usuario

## Introducción

SAMLAssertionGen es una herramienta que permite la generación de SAML Assertions para autenticación y obtención de tokens en aplicaciones que requieren este tipo de autenticación.

Este proyecto está basado en Maven y requiere configuraciones previas para su correcta ejecución.

## Prerrequisitos

Antes de ejecutar el proyecto, asegúrese de contar con lo siguiente:

- **Java 8 o superior** instalado en el sistema.
- **Maven** instalado y configurado correctamente.
- **Conexión a Internet** para descargar dependencias de Maven.

## Estructura del Proyecto

El proyecto está compuesto por los siguientes archivos y carpetas principales:

```
SAMLAssertionGen/
├── src/                    # Contiene el código fuente del proyecto.
├── pom.xml                 # Archivo de configuración de Maven con las dependencias requeridas.
├── SAMLAssertion.properties # Archivo de configuración para la generación de la SAML Assertion.
├── config.properties       # Archivo de configuración adicional con datos de autenticación.
```

## Configuración de los Archivos de Propiedades

### Archivo `SAMLAssertion.properties`

Antes de ejecutar el proyecto, es necesario modificar el archivo `SAMLAssertion.properties` con la información adecuada:

- **tokenUrl**: URL donde se solicitará el token.
- **clientId**: Identificador del cliente registrado en la aplicación BizX.
- **userId / userName**: Usuario para el cual se generará la aserción SAML.
    - Si `userId` está vacío o nulo, se utilizará `userName`.
- **privateKey**: Clave privada que coincide con la clave pública configurada en BizX.
- **expireInMinutes**: Tiempo en minutos de validez de la SAML Assertion.

Ejemplo de archivo `SAMLAssertion.properties`:

```properties
tokenUrl=https://your-token-url.com
clientId=your-client-id
userId=your-user-id
privateKey=your-private-key
```

### Archivo `config.properties`

Este archivo contiene configuraciones adicionales necesarias para la autenticación.

Ejemplo de archivo `config.properties`:

```properties
tokenUrl=https://your-authentication-url.com
clientId=your-client-id
companyId=your-company-id
```

## Generación de la SAML Assertion

Existen dos formas de generar la SAML Assertion:

### Opción 1: Ejecución directa con Maven

Ejecute el siguiente comando en la carpeta del proyecto:

```sh
mvn compile exec:java -Dexec.args="SAMLAssertion.properties"
```

### Opción 2: Compilar y ejecutar el JAR manualmente

#### 1. Compilar y Empaquetar el Proyecto

Ejecute el siguiente comando en la carpeta del proyecto:

```sh
mvn clean compile package
```

Una vez completado el proceso, se generará una carpeta `target/` con el archivo `SAMLAssertionGen-1.0.0.jar`.

#### 2. Preparar Archivos

Copie los siguientes archivos al mismo directorio:

- `SAMLAssertionGen-1.0.0.jar`
- `SAMLAssertion.properties`
- `config.properties`

#### 3. Ejecutar el JAR

Ejecute el siguiente comando para generar la aserción SAML:

```sh
java -jar target/SAMLAssertionGen-1.0.0.jar "SAMLAssertion.properties"
```

## Resultado Esperado

Si la ejecución es exitosa, en la línea de comandos se mostrará el siguiente mensaje:

```sh
The generated Signed SAML Assertion is: <SAML Assertion Base64>
```

Si además se obtiene un OAuth Token, se mostrará un mensaje similar a:

```json
OAuth Token Response:

{"access_token":"eyJ0b2tlbkNvbnRlbnQiOnsiYXBpS2V5IjoiTTJZMVlq...","token_type":"Bearer","expires_in":80985}
```

Para decodificar la SAML Assertion en Base64 y verificar su contenido:

```sh
echo "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNv..." | base64 --decode
```
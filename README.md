# Generador de SAML Assertions y Tokens OAuth2

## Introducción

SAML Assertions es una herramienta que permite la generación de SAML Assertions para autenticación y obtención de tokens en aplicaciones que requieren este tipo de autenticación.

Este proyecto está desarrollado en **Java** y utiliza **Maven** para la gestión de dependencias.

## Prerrequisitos

Antes de ejecutar el proyecto, asegúrese de contar con lo siguiente:

- **Java 17** instalado en el sistema.
- **Maven** instalado y configurado correctamente.
- **Conexión a Internet** para descargar dependencias de Maven.

## Configuración y Uso del Generador de SAML Assertion

En este proyecto, la configuración puede realizarse de diferentes maneras según las necesidades del usuario. Se pueden proporcionar parámetros directamente en el código, pasarlos como argumentos en la línea de comandos o configurar variables de entorno para mayor seguridad y flexibilidad.

Ejemplo de configuración en código:

```java
public class SAMLAssertionGenerator {
    public static void main(String[] args)  {
        GenerateSaml generator = new GenerateSaml();

        String token = generator.generatesaml(
                "https://example.com/oauth/token",
                "your-client-id",
                "your-private-key",
                "your-user-api",
                "your-company-id"
        );
        System.out.println("Generated OAuth Token: " + token);
    }
}
```

### Opción 1: Ejecución con Maven

Ejecute el siguiente comando en la carpeta del proyecto:

```sh
mvn compile exec:java
```

### Opción 2: Compilar y ejecutar manualmente el JAR

#### 1. Compilar y empaquetar el proyecto

```sh
mvn clean compile package
```

Esto generará una carpeta `target/`: y el archivo JAR 

```
target/SAMLAssertionGen-1.0.0.jar
```

#### 2. Ejecutar el JAR

Ejecute el siguiente comando:

```sh
java -jar target/SAMLAssertionGen-1.0.0.jar
```

## Resultado Esperado

Si la ejecución es exitosa, se mostrará un mensaje como el siguiente:

```sh
The generated Signed SAML Assertion is: <SAML Assertion Base64>
```

Si además se obtiene un OAuth Token, se mostrará un JSON similar a:

```json

Generated OAuth Token:

"eyJ0b2tlbkNvbnRlbnQiOnsiYXBpS2V5IjoiTTJZMVlq...",

```

Para decodificar la SAML Assertion en Base64 y verificar su contenido:

```sh
echo "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNv..." | base64 --decode
```

## Notas Adicionales

- Asegúrese de que la clave privada utilizada sea válida y coincida con la clave pública configurada en el proveedor de identidad.
- No incluya credenciales reales en el código fuente. Para mayor seguridad, considere el uso de variables de entorno o un servicio de gestión de secretos.
- Si encuentra errores de autenticación, revise que los valores en el código sean correctos y que el servicio de autenticación esté disponible.

---


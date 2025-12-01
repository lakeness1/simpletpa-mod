Plan de Implementación V2: SimpleTPA (Advanced)
===============================================

**Autor:** Lake & Asistente**Versión:** Forge 1.20.1**Cambios:** Configuración Global + Toggle + Comentarios Mejorados

1\. Reingeniería de la Configuración
------------------------------------

Vamos a cambiar el tipo de configuración para que el archivo se genere en la raíz.

### A. Cambio de Ubicación (Global Config)

En lugar de SERVER (que es por mundo), usaremos COMMON. Esto crea el archivo en la carpeta /config/ de la raíz del servidor.

*   // Antes:// context.registerConfig(ModConfig.Type.SERVER, TpaConfig.SPEC);// Ahora:ModConfigContext.get().registerConfig(ModConfig.Type.COMMON, TpaConfig.SPEC, "simpletpa-common.toml");
    

### B. Comentarios y Placeholders (Documentación interna)

Usaremos el método .comment() del ForgeConfigSpec.Builder para inyectar instrucciones claras directamente en el archivo TOML.

**Estructura de la Clase TpaConfig:**

1.  **Definición de Variables:**
    
    *   Usar bloques de comentarios multilínea.
        
    *   Explicar qué hace cada %s (String) o %d (Número).
        

**Ejemplo de implementación de comentario:**

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   msgRequestSent = builder      .comment("Mensaje enviado al jugador que inicia el comando /tpa.",               "Placeholders:",               "  %s - Nombre del jugador objetivo (quien recibe la solicitud).",               "Usa '&' para códigos de color (ej: &6 para dorado).")      .define("msgRequestSent", "&eSolicitud enviada a &6%s");   `

2\. Nueva Característica: Sistema Toggle (Ignorar Solicitudes)
--------------------------------------------------------------

Permitiremos que los jugadores bloqueen las solicitudes entrantes.

### A. Almacenamiento de Estado

Para mantenerlo simple y ligero (sin base de datos), usaremos un HashSet en memoria dentro del TpaManager.

*   _Nota:_ Al ser en memoria, si el servidor se reinicia, el bloqueo se resetea (vuelven a aceptar solicitudes). Esto es el comportamiento estándar en muchos plugins ligeros.
    

**En TpaManager:**

*   **Variable:** private static final Set ignoringPlayers = new HashSet<>();
    
*   **Método:** toggleIgnore(UUID playerUuid)
    
    *   Si está en el Set -> Lo quita (Ahora acepta). Retorna true.
        
    *   Si no está -> Lo añade (Ahora ignora). Retorna false.
        
*   **Método:** isIgnoring(UUID playerUuid) -> Retorna boolean.
    

### B. Comando /tpa toggle

1.  Obtener el jugador ejecutor.
    
2.  Llamar a TpaManager.toggleIgnore(uuid).
    
3.  Enviar mensaje configurable: _"Modo ignorar solicitudes: \[ACTIVADO/DESACTIVADO\]"_.
    

### C. Modificación de Comandos /tpa y /tpahere

Antes de crear la TeleportRequest, debemos verificar el estado del objetivo.

**Lógica actualizada:**

1.  Jugador A escribe /tpa JugadorB.
    
2.  Verificar si JugadorB está online.
    
3.  **Nueva Verificación:** if (TpaManager.isIgnoring(JugadorB.getUUID()))
    
    *   Error a Jugador A: _"Este jugador tiene las solicitudes desactivadas."_
        
    *   Cancelar operación.
        
4.  Si no ignora, proceder normalmente.
    

3\. Resumen de Estructura de Archivos Actualizada
-------------------------------------------------

Así quedará tu proyecto con estos cambios:

*   src/main/java/com/lake/simpletpa/
    
    *   SimpleTPA.java (Main: Registra Config COMMON y Comandos).
        
    *   config/TpaConfig.java (Define variables y comentarios detallados).
        
    *   commands/TpaCommand.java (Lógica de Brigadier para tpa, tpahere, accept, deny, toggle).
        
    *   manager/TpaManager.java (Maneja el Map de solicitudes y el Set de ignorados).
        
    *   manager/TeleportRequest.java (Objeto de datos).
        
    *   util/MessageUtils.java (Ayuda a formatear colores y reemplazar %s).
        

4\. Ejemplo del Archivo TOML Resultante
---------------------------------------

Gracias a los cambios, el usuario verá esto al abrir el archivo:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   [messages]      # Mensaje enviado al jugador que inicia el comando /tpa.      # Placeholders:      #   %s - Nombre del jugador objetivo.      # Usa '&' para códigos de color.      msgRequestSent = "&eSolicitud enviada a &6%s"      # Mensaje cuando intentas enviar tpa a alguien que tiene el toggle activo.      msgTargetIgnoring = "&cEse jugador no está aceptando solicitudes."   `

5\. Siguientes Pasos en el Código
---------------------------------

1.  **Rama Git:** Asegúrate de estar en feature/config-update (o crea una nueva si prefieres).
    
2.  **Config:** Empezaremos creando la clase TpaConfig con los comentarios detallados.
    
3.  **Manager:** Añadiremos el Set para el toggle.
    
4.  **Comandos:** Implementaremos la lógica de bloqueo.
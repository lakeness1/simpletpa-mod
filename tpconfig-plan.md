Plan de Implementación: Configuración y Warmup
==============================================

**Autor:** Lake & Asistente**Contexto:** Extensión del mod SimpleTPA (Forge 1.20.1)**Objetivo:** Crear un archivo de configuración (simpletpa-server.toml) para mensajes y tiempos.

1\. El Sistema de Configuración (ForgeConfigSpec)
-------------------------------------------------

En lugar de tener valores "hardcoded" (fijos en el código), usaremos un Builder de Forge para definir variables que se escribirán en un archivo TOML.

### Clase: TpaConfig

Esta clase contendrá tanto la definición de la configuración como los valores accesibles.

#### Estructura Propuesta

Se dividirá en dos categorías: General y Messages.

**A. Categoría General (Tiempos)**

*   tpExpirationTime: (Int) Segundos que dura una solicitud antes de caducar. _Default: 120_.
    
*   teleportWarmup: (Int) Segundos que el jugador debe esperar sin moverse tras aceptar. _Default: 3_.
    
*   cooldown: (Int) Segundos que debe esperar un jugador entre usos del comando. _Default: 0_.
    

**B. Categoría Messages (Traducción)**Aquí definiremos todos los textos. Soportaremos códigos de color de Minecraft (usando & o §).

*   msgRequestSent: "Solicitud enviada a %s".
    
*   msgRequestReceived: "%s quiere teletransportarse...".
    
*   msgTeleporting: "Teletransportando...".
    
*   msgWarmup: "No te muevas durante %d segundos...".
    
*   msgWarmupCancel: "Te has movido. Teletransporte cancelado.".
    

### Registro en la Clase Principal

En el constructor de tu clase principal (Main), debes registrar la configuración:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TpaConfig.SPEC, "simpletpa-server.toml");   `

_Nota: Usar ModConfig.Type.SERVER creará el archivo dentro de la carpeta serverconfig de cada mundo (save), lo cual es ideal para configuraciones que afectan la lógica del juego._

2\. Implementación del "Warmup" (Tiempo de Espera)
--------------------------------------------------

El "Warmup" complica un poco el flujo: ya no teletransportamos inmediatamente al aceptar.

### Nueva Estructura: WarmupManager

Necesitamos un gestor temporal para los jugadores que están "esperando" ser teletransportados.

*   **Mapa:** Map warmingUpPlayers
    
*   **Clase WarmupTask:**
    
    *   UUID playerUuid: Quien se va a mover.
        
    *   UUID targetUuid (opcional): Hacia quién va (o coordenadas destino).
        
    *   long startTime: Cuándo empezó a esperar.
        
    *   Vec3 startPos: Posición inicial (para detectar movimiento).
        

### Modificación del Flujo /tpaccept

1.  **Antes:** /tpaccept -> player.teleportTo(...).
    
2.  **Ahora:** /tpaccept ->
    
    *   Verificar configuración teleportWarmup.
        
    *   **Si es 0:** Teletransportar inmediatamente.
        
    *   **Si es > 0:**
        
        *   Crear WarmupTask.
            
        *   Guardar posición actual del jugador que va a viajar.
            
        *   Enviar mensaje: "Quédate quieto %d segundos".
            
        *   Añadir al WarmupManager.
            

### Lógica de Detección (TickHandler)

En el mismo ServerTickEvent (o uno nuevo dedicado):

1.  Iterar sobre warmingUpPlayers.
    
2.  **Verificar Movimiento:**
    
    *   Si player.position().distanceTo(task.startPos) > 0.5:
        
        *   Cancelar tarea.
            
        *   Mensaje: "Te moviste, cancelado".
            
3.  **Verificar Tiempo:**
    
    *   Si currentTime > task.startTime + (config.warmup \* 1000):
        
        *   Ejecutar teletransporte.
            
        *   Eliminar tarea.
            
        *   Mensaje: "Teletransportando...".
            

3\. Integración de Mensajes Configurables
-----------------------------------------

Debes crear una clase utilitaria, por ejemplo MessageUtils, para leer la config y formatear colores.

*   **Método:** send(ServerPlayer player, ForgeConfigSpec.ConfigValue configMsg, Object... args)
    
*   **Lógica:**
    
    1.  Obtener String de la config: configMsg.get().
        
    2.  Reemplazar %s o %d con los argumentos (nombre del jugador, segundos, etc.).
        
    3.  Convertir & a códigos de color reales.
        
    4.  Enviar al jugador usando player.sendSystemMessage(...).
        

4\. Resumen de Cambios en Código Existente
------------------------------------------

1.  **TeleportRequest**: Ahora usará TpaConfig.tpExpirationTime.get() en lugar de 120 fijo.
    
2.  **TpaManager**: Al procesar /tpaccept, delegará al WarmupManager si la configuración lo dicta.
    
3.  **Comandos**: Reemplazar todos los Component.literal("Texto") por llamadas a tu sistema de configuración de mensajes.
    

5\. Ejemplo de Archivo Generado (Preview)
-----------------------------------------

Una vez implementado, al iniciar el servidor se generará esto automáticamente:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   [general]      # Tiempo en segundos para que expire una solicitud      # Range: > 1      requestExpiration = 120      # Tiempo de espera antes de teletransportar (Warmup)      # Range: 0 ~ 60      teleportWarmup = 3  [messages]      requestSent = "&eSolicitud enviada a &6%s"      teleporting = "&aTeletransportando..."   `
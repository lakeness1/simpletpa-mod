Implementation Plan: SimpleTPA (Server-Side)
============================================

**Author:** Lake & Assistant**Minecraft Version:** Forge 1.20.1**Mod Type:** Server-Side Only

Introduction
------------

This document details the implementation plan for the "SimpleTPA" mod. The goal is to create a teleport request system (/tpa, /tpahere) that works exclusively on the server, allowing _vanilla_ players (without mods) to connect and use it seamlessly.

1\. Environment Configuration (Crucial)
---------------------------------------

To ensure the mod is "Server-Side Only", it is necessary to modify the build configuration so the server does not enforce this mod on clients.

### File: src/main/resources/META-INF/mods.toml

You must locate the displayTest property and configure it to ignore the server version. This allows vanilla clients to join.

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   # mods.toml  displayTest="IGNORE_SERVER_VERSION"   `

2\. Data Structure (The Model)
------------------------------

We will not use a database to keep it lightweight. We will use a simple Java class to store requests in RAM (Volatile memory).

### Class: TeleportRequest

This class will represent an individual request.

*   **UUID sender**: Identifier of the player sending the request.
    
*   **UUID target**: Identifier of the player receiving the request.
    
*   **RequestType type**: Defines the direction of the teleport (TPA or TPA\_HERE).
    
*   **long expirationTime**: Timestamp (milliseconds) when the request expires.
    
    *   _Formula:_ System.currentTimeMillis() + (120 \* 1000) (2 minutes).
        

### Enum: RequestType

*   TPA: The Sender travels to the Target.
    
*   TPA\_HERE: The Target travels to the Sender.
    

3\. Management Logic (Manager)
------------------------------

We need a centralized manager (Singleton or static) to manage the lifecycle of requests.

### Class: TpaManager

*   **Storage:** Map pendingRequests
    
    *   **Key:** We will use the **target's** UUID (the receiver). This makes it easy to check "Do I have any requests?" when the player types /tpaccept.
        
*   **Functional Methods:**
    
    1.  **addRequest(request)**: Adds a request to the map. If the player already had a pending one, it overwrites it.
        
    2.  **getRequest(targetUUID)**: Retrieves the pending request for that player.
        
    3.  **removeRequest(targetUUID)**: Removes the request (upon accept, deny, or expiration).
        
    4.  **cleanExpired()**: Maintenance method to purge old requests.
        

4\. Commands (Brigadier)
------------------------

Commands are registered in the RegisterCommandsEvent event.

### A. Command /tpa

*   **Function:** Requests to go to another player's position.
    
*   **Flow:**
    
    1.  Check if the target is online.
        
    2.  Create TeleportRequest (Type TPA).
        
    3.  Send message to Target with clickable buttons (if possible) or text instructions: _"Type /tpaccept to accept"_.
        

### B. Command /tpahere

*   **Function:** Requests another player to come to your position.
    
*   **Flow:**
    
    1.  Identical to the previous one, but the RequestType is TPA\_HERE.
        
    2.  Message to Target changes: _"X wants you to teleport to them"_.
        

### C. Command /tpaccept

*   **Function:** Accepts the pending request.
    
*   **Flow:**
    
    1.  Search for request in TpaManager using the executor's UUID.
        
    2.  If it doesn't exist -> Error.
        
    3.  If it exists -> Check type.
        
        *   If TPA: Teleport Sender -> Target.
            
        *   If TPA\_HERE: Teleport Target -> Sender.
            
    4.  Remove request.
        

### D. Command /tpdeny

*   **Function:** Rejects the request.
    
*   **Flow:**
    
    1.  Find and remove the request from TpaManager.
        
    2.  Notify the sender that it was rejected.
        

5\. Expiration System (Tick Handler)
------------------------------------

To automatically cancel requests after 2 minutes without using complex threads, we hook into the server tick loop.

### Event: ServerTickEvent

*   This event occurs 20 times per second (20 TPS).
    
*   **Optimization:** Do not check every tick. Use server.getTickCount() % 20 == 0 to check only once per second.
    
*   **Action:** Call TpaManager.cleanExpired().
    
    *   If now > expiration\_time:
        
        *   Delete request.
            
        *   Send message to Sender and Target: _"The teleport request has expired"_.
            

6\. Technical Considerations
----------------------------

*   **Dimensions:** When teleporting, use the method that includes ServerLevel. If one player is in the Nether and another in the Overworld, moving only X/Y/Z coordinates will result in death or bugs.
    
    *   _Correct method:_ entity.teleportTo(serverLevel, x, y, z, yaw, pitch)
        
*   **Persistence:** Since these are temporary requests, it is not necessary to save anything to disk when the server shuts down. If the server restarts, requests are lost, which is acceptable behavior.
    

7\. Programming Steps
---------------------

1.  **Setup:** Configure mods.toml.
    
2.  **Core:** Create TeleportRequest and TpaManager classes.
    
3.  **Commands:** Implement the basic command structure with Brigadier.
    
4.  **Logic:** Connect commands to the Manager.
    
5.  **Events:** Register the ServerTickEvent for cleanup.
    
6.  **Test:** Test with a vanilla client and a modded client (or two vanilla clients on LAN/Dedicated Server).
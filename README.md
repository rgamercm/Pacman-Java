# 🎮 PAC-MAN ERIKIKI - Proyecto Java con Swing

![Pac-Man Logo](https://upload.wikimedia.org/wikipedia/commons/7/7d/PAC-MAN_logo.png)

Un clásico juego de Pac-Man implementado en Java con Swing, perfecto para aprender programación orientada a objetos y desarrollo de juegos.

## 🌟 Características

- ✅ Menú principal interactivo
- ✅ Selección de color para Pac-Man
- ✅ Sistema de puntuación y vidas
- ✅ Fantasmas con diferentes comportamientos
- ✅ Efectos de sonido
- ✅ Pausa y reinicio del juego
- ✅ Pantallas de Game Over y victoria

## 🛠 Requisitos

### 1. Instalar Java JDK (Requisito principal)
**Para Venezuela:** Usa VPN para acceder a Oracle

🔗 [Descargar Java JDK](https://www.oracle.com/java/technologies/downloads/?er=221886#jdk24-windows)

### 2. Instalar Visual Studio Code (IDE recomendado)

🔗 [Descargar VS Code](https://code.visualstudio.com/)

### 3. Configurar Java en VS Code

Sigue la guía oficial:
🔗 [Java en VS Code](https://code.visualstudio.com/docs/languages/java)

## 🚀 Configuración del Proyecto

### Verificar instalación de Java
Abre una terminal y ejecuta:
```bash
java –version
```

Deberías ver algo como:
```
java version "17.0.10" 2024-01-16 LTS
```

### Crear proyecto en VS Code
1. Abre VS Code
2. Presiona `Ctrl+Shift+P` y escribe:
   ```
   >Java: Create Java Project
   ```
3. Selecciona "No build tools"
4. Elige la carpeta y nombre del proyecto

### 🔥 Solución rápida si javac no funciona

1. Encuentra la ruta de instalación de tu JDK:
   - Generalmente en:
     ```
     C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot\bin
     ```
   - O busca `jdk-17` en:
     - `C:\Program Files\Eclipse Adoptium\`
     - `C:\Program Files\Java\`

2. Agrega la ruta al PATH:
   - Busca "Editar variables de entorno" en Windows
   - En "Variables del sistema", edita PATH
   - Agrega la ruta del `bin` de tu JDK
   - Ejemplo: `C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot\bin`

3. Verifica en una terminal nueva:
   ```bash
   javac -version
   ```
   Deberías ver:
   ```
   javac 17.0.15
   ```

## 🧩 Estructura del Proyecto

```
pacman-erikiki/
├── src/
│   ├── PacMan.java         # Clase principal del juego
│   ├── SoundManager.java   # Manejo de sonidos
│   └── resources/          # Imágenes y sonidos
│       ├── pacman_amarillo/
│       ├── blueGhost.png
│       ├── wall.png
│       └── ...
└── README.md
```

## 🎯 Conceptos de POO Aplicados

### 1. Clases Principales
```java
public class PacMan extends JPanel implements ActionListener, KeyListener {
    // Lógica principal del juego
}

class Block {
    // Representa elementos del juego (Pac-Man, fantasmas, paredes)
}

class SoundManager {
    // Maneja reproducción de sonidos
}
```

### 2. Estados del Juego
```java
enum GameState {
    MAIN_MENU,       // Menú principal
    COLOR_SELECTION, // Selección de color
    PLAYING,         // Juego en curso
    PAUSED,          // Juego pausado
    GAME_OVER,       // Fin del juego
    PLAYER_DIED      // Jugador perdió una vida
}
```

### 3. Elementos Clave

| Componente       | Descripción                                  |
|------------------|---------------------------------------------|
| `Block`          | Molde para Pac-Man, fantasmas y paredes     |
| `tileMap`        | Diseño del laberinto en texto               |
| `movePacman()`   | Lógica de movimiento del jugador            |
| `moveGhosts()`   | Comportamiento de los fantasmas             |
| `paintComponent` | Dibuja todos los elementos gráficos         |

## 🕹️ Cómo Jugar

- **Teclas**: 
  - Flechas o WASD para mover a Pac-Man
  - `P` para pausar/reanudar
  - `ESC` para volver al menú desde pausa
- **Objetivo**: 
  - Come todos los puntos
  - Evita a los fantasmas
  - ¡Supera tu puntuación máxima!

## 📚 Aprendizaje

Este proyecto es perfecto para aprender:

- Programación Orientada a Objetos
- Manejo de gráficos con Swing
- Colisiones y física simple
- Máquinas de estados (GameState)
- Manejo de eventos de teclado
- Diseño de arquitectura de juegos

## 🤝 Contribuir

¡Contribuciones son bienvenidas! Haz fork del proyecto y envía tus mejoras.

## 📜 Licencia

MIT License - Libre para uso educativo y personal.

---

<div align="center">
  <h3>¡Diviértete programando y jugando!</h3>
  <img src="https://media.giphy.com/media/XbxZ41fWLeRECPsGIJ/giphy.gif" width="200">
</div>

# Product Requirements Document (PRD)
## Project Code Name: ManhwaAI Scroll (FX Scroll Reader)
**Author:** Google AI Studio Coding Agent  
**Date:** 2026-06-05  
**Context:** Mobile-First Webtoon Experience with Procedural Audio-Visual Generation  

---

## 1. Executive Summary & Vision

Traditional webtoon and manhwa platforms display static visual panels designed to be consumed by vertical scrolling. While the vertical layout is ergonomic for single-handed mobile navigation, it fails to capitalize on the rich multimedia capabilities of modern mobile devices. 

**ManhwaAI** introduces a paradigm shift. It translates vertical scroll physical velocity, progress, and acceleration into a real-time, context-aware performance. By feeding uploaded manhwa vertical strips through an AI analytics pipeline (powered by **Gemini-3.5-Flash**), the system decomposes image strips into panels, registers focal objects, determines the emotional beats, and generates coordinates for real-time visual-audio orchestration. The results are injected seamlessly during the user's reading experience:
- **Procedural Background Orchestration**: Dynamic music generated on-the-fly, smoothly interpolating tempos, keys, and motifs to match panel transitions.
- **Context-Bound Screen Shakes & Cinematic Scale Modifiers**: Shaking, zooming, panning, or tinting individual panels based on collision depth or scroll-focus.
- **Onomatopoeia Pop Effects**: Graphic text overlays (e.g., *Swoosh*, *Crash*) animated dynamically over appropriate coordinates with physical decay.
- **Dynamic Expression Aura Particles**: Particle generators rendering context overlays (e.g., speed lines for action, sweat particles for suspense, sparkling hearts for romance) to emphasize character state.

This mobile-first experience retains the offline accessibility of standard readers while delivering the immersive weight of animated cinema.

---

## 2. User Personas

1. **The Hardcore Reader ("Gamer & Shonen Enthusiast")**  
   - *Need*: Desires maximum visceral weight of fighting scenes; wants screen shake, heavy metal or epic orchestral swells on heavy impacts, and fast cinematic speed lines.
2. **The Comfort Scholar ("Casual Drama/Slice-of-life Reader")**  
   - *Need*: Prefers slow-burning ambient atmospheres, gentle falling particles, and seamless acoustic melodies that adjust to visual cues without distracting.
3. **The Independent Content Creator ("Uploader")**  
   - *Need*: Wants to upload raw comic panels/strips and have an automated AI pipeline automatically tag, enrich, and sound-design their project immediately without manual editing.

---

## 3. Product Architecture & AI Pipeline Flow

The system is split into two major conceptual components: the **Ingestion/AI Enrichment Pipeline** and the **Dynamic Scroll Render Engine**.

```
+----------------------------------------------------------------------------+
|                          1. INGESTION & UPLOAD STEP                        |
|                                                                            |
|  Uploader selects a Manhwa Strip (PNG/JPG) -> Captured and Prepared        |
+------------------------------------+---------------------------------------+
                                     |
                                     v
+------------------------------------+---------------------------------------+
|                       2. GEMINI PIPELINE RECONNAISSANCE                    |
|                                                                            |
|  Image uploaded to Gemini API along with Multi-Modal parsing prompt.        |
|  Returns structured JSON identifying panels, bounding boxes, text, details: |
|   - Panel Segments (Y-offsets & Heights in scale coordinates)               |
|   - Scene Intent: Mood (Romance, Horror, Epic Action, Mystery)             |
|   - Acoustic Soundscape Profile: Theme chords, tempo (BPM), primary synth   |
|   - Onomatopoeia Tags: Text, X/Y-Coordinates, trigger intensity, SFX sound   |
|   - Screen Overlays: FX particles (fire, hearts, speed lines, rain, sweat) |
+------------------------------------+---------------------------------------+
                                     |
                                     v
+------------------------------------+---------------------------------------+
|                    3. ROOM DATABASE PERSISTED STORAGE                      |
|                                                                            |
|  Stores: Manhwa Metadata, Panel coordinates, parsed Soundscape configurations, |
|  Onomatopoeia registers, and Visual Overlay tags.                          |
+------------------------------------+---------------------------------------+
                                     |
                                     v
+------------------------------------+---------------------------------------+
|                    4. DYNAMIC SCROLL RENDER ENGINE                         |
|                                                                            |
|  Mobile Webtoon Reader continuously tracks viewport scroll metrics:         |
|   - Calculate "Active Focused Panel" using scroll center intersections.    |
|   - Interpolates audio synthesizer synthesizer settings (BPM, filter cutoffs)|
|   - Triggers dynamic SFX sounds upon focal intersection milestones.        |
|   - Activates specific panel animations (shakes, zooms).                   |
|   - Runs real-time Canvas particles (hearts, auras, fire) over the strip.  |
+----------------------------------------------------------------------------+
```

---

## 4. Feature Specifications (Mobile-First)

### 4.1. Library Dashboard & Upload Mock Ingestion
- **Dashboard**: High-fidelity carousel showing available manhwas. Interactive filter tags (Action, Romance, Mystery).
- **Upload Flow Integration**: 
  - Allows mock upload or prompt generation to simulate uploader work.
  - Interactive "AI Analyzer Dashboard" displaying live pipeline steps: `Segmenting Panels` -> `Extracting Audio Moods` -> `Synthesizing Soundscapes` -> `Generating Visual Overlay Cues`.
  - Sends requests to the actual underlying Gemini API using Retrofit. Parses the return block into Room records.

### 4.2. Adaptive Audio Synthesizer (Music & SFX Engine)
To maintain an extreme lightweight profile and bypass large audio file dependencies:
- **Procedural Synth Engine**: Uses Android’s `AudioTrack` and Kotlin oscillators to synthesize music in real-time.
- **Symphonic Mood Signatures**:
  - **Epic Action**: Fast tempo (130 BPM), minor key triads, saw-tooth aggressive pitch slides (epic synth brass), high-frequency energy.
  - **Horror / Suspense**: Slow tempo (60 BPM), dissonant diminished intervals, low sine rumbles, high-pass filter noise.
  - **Fantasy Romance**: Comfort tempo (80 BPM), dreamy arpeggios in major 7th chords, sweet sine wave pads.
  - **Mysterious Forest / Serene**: Soft wind noise simulation, minor pentatonic woodwind-like melodic cues.
- **Scroll Hooked Interpolation**: When the user scrolls, the speed of notes or pitch cutoff changes proportionally.
- **Dynamic Onomatopoeia SFX**: Synthesizes custom synthesized impacts like metallic impact waves for *CLANG*, explosive square-wave sweeps for *BOOM*, and pitch-swept white noise for *SWISH*.

### 4.3. Vertical Webtoon Reader with Frame Focus Hooks
- **Scroll Focus Tracker**: Computes active visual intersection ratio.
- **Cinematic Scene Triggers**:
  - **Screen Shakes**: Uses offset multipliers based on panel's action value.
  - **Color Grading Tint Overlays**: Radial gradient vignettes (e.g. flashing red blood-vignette for horror, warm pink glow for romance).
  - **Canvas Particle Matrix**: Real-time rendering of falling visual items overlayed on the phone's screen canvas.

---

## 5. Software Architecture & Database Schema

The implementation is structured around **Clean Architecture MVVM Pattern**:

### 1. Database Schema (Room)
- `ManhwaEntity`: `id`, `title`, `author`, `genre`, `description`, `coverUrl`, `isProcessed`, `imageSeed`
- `PanelEntity`: `id`, `manhwaId`, `panelIndex`, `topOffsetPercent`, `bottomOffsetPercent`, `mood`, `description`
- `OnomatopoeiaEntity`: `id`, `panelId`, `text`, `xPercent`, `yPercent`, `sfxType`, `scale`
- `VisualFxEntity`: `id`, `panelId`, `fxType` (SPEED_LINES, SWEAT, SPARKS, FIRE, SANITY_DECAY), `intensity`

### 2. Audio Processing Pipeline
Uses a dedicated service worker singleton `ProceduralAudioEngine.kt` executing customized coroutine loops that update oscillator parameters.

---

## 6. Technical Stack & Feasibility

- **Language**: Kotlin 1.9+ / Kotlin 2.2
- **UI Platform**: Jetpack Compose (Kotlin declarative structure)
- **Database**: Android Room Persistence Library with reactive flows.
- **Image Processing / Mock Loader**: Coils Compose, customized Jetpack Compose `Canvas` drawing vectors.
- **AI Backend**: **Gemini-3.5-Flash** via direct REST endpoints. (Fallback to highly adaptive local heuristic parsing generator if API network fails or API Key is unset).
- **Sound Service**: Android Low-Level OpenSL ES or standard JVM Java `AudioTrack` output stream playing PCM arrays synthesized dynamically in Kotlin. Highly feasible and highly innovative!

---

## 7. Metrics & Success Indicators

1. **User Immersion Duration**: Average time reading a manhwa (higher immersion translates to slower, more controlled scrolling to absorb details).
2. **Dynamic Sync Fluidity**: Frame rate of the scroll reader during rendering of audio-visual panels (Target: stable 60 FPS on mid-range Android devices).
3. **Pipeline Accuracy**: Gemini categorizations correctly aligned with comic intent (evaluated through offline validation testing).

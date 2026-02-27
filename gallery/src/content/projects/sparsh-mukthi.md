---
title: "Sparsh Mukthi"
teamName: "Code Krafters"
thumbnail: "/images/sparsh-mukthi-gallery.jpg"
type: "Medical"
description: "Sparsh Mukthi allows users to **control their system without touching it"
isFinalist: false 
---

# Sparsh Mukthi


## Inspiration
In today’s world, constant **physical interaction with devices**—whether keyboards, mice, or touchscreens—creates inefficiencies and hygiene concerns, especially in sensitive environments such as **hospitals, VR classrooms, and shared office spaces**.

This inspired us to build **Sparsh Mukthi**, a system that enables **touchless control** through a combination of **hand gestures and voice commands**.

Our vision is to create a more **hygienic, accessible, and futuristic interface** that reduces touch dependency and expands inclusivity for differently-abled users.

---

## What it does
Sparsh Mukthi allows users to **control their system without touching it**, by:

* Recognizing **voice commands** to perform common actions (e.g., opening apps, sending messages, searching online).
* Detecting **hand gestures** to navigate screens, scroll, or interact with applications.
* Offering **context-aware automation**, so commands adapt based on the active screen (e.g., WhatsApp vs. Chrome).

This blend of gesture and speech creates a **seamless human–computer interaction** model.

---

## How we built it
* **Frontend:** Minimalistic Python interface for voice & gesture command execution.
* **Voice Recognition:** Integrated speech-to-text APIs for accurate and fast command understanding.
* **Gesture Recognition:** Implemented computer vision techniques (OpenCV/MediaPipe) to map hand movements into actionable commands.
* **Context Awareness:** Designed logic to adapt voice/gesture inputs depending on the **active application window**.
* **AI Layer:** Ensured natural language understanding so users can interact without rigid command syntax.

---

## Challenges we ran into
1. **Noise Sensitivity in Voice Recognition:** Background noise often interfered with voice commands.
    * *Solution:* Applied noise cancellation filters and adjusted thresholds for accuracy.
2. **Gesture Accuracy in Low Light:** Hand tracking became unstable in poor lighting conditions.
    * *Solution:* Added preprocessing filters and fallback command options.
3. **Context Switching:** Ensuring that the assistant understood **different app environments** (e.g., WhatsApp vs. browser) was non-trivial.
    * *Solution:* Designed a modular context-handling engine to dynamically interpret actions.

---

## Accomplishments that we're proud of
* Built a **working prototype** that blends **voice and gesture recognition** into one system.
* Achieved **cross-environment adaptability**, making it usable across multiple applications.
* Designed with a focus on **accessibility**, ensuring it benefits differently-abled communities.
* Created a **touchless, hygienic interaction model** that has direct use cases in healthcare and education.

---

## What we learned
* The importance of **human-centered design** in AI projects.

## Built With
`css`, `flask`, `html`, `javascript`, `mediapipe`, `numpy`, `opencv`, `pyautogui`, `pygame`, `pynput`, `python`, `sciket-learn`

## Try it out
* [sparsh-mukthi.vercel.app](https://sparsh-mukthi.vercel.app/)
* [sparsh-mukthi.xyz](http://sparsh-mukthi.xyz/)
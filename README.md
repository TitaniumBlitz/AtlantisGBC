# AtlantisGBC
A collection of homebrew utilities for GB/GBC homebrewers.

There are a lot of utilities spread across the web that enable individuals to write and modify games for the gameboy and gameboy color platforms. However, these utilities can be hard to find and include fees to allow access to sophisticated features. The goal of this project is to provide one entire package featuring all of the essential tools to enable gb/gbc enthusiasts to create, modify, debug, emulate and perform a mryiad of other miscellaneous transformations to gb and gbc programs.

These utilities will run on any system capable of running the Java runtime. The code will require no additional 3rd-party extensions or libraries other than the native libraries already packed and supported by your specific architecture's jdk/jre.

Ultimately, the following utilities will be featured in the AtlantisGBC package:

- A fast GB/GBC emulator & debugger
- A GB/GBC disassembler 
- A Z80 assembler (with gb/gbc specific extensions) 
- An onboard code injection window/tool to allow users to inject Z80 code assembled by the assembler directly into any GB/GBC game at any point during runtime that they desire.
- A tool to extract sprites and themes from gameboy and gbc games with the ability to perform various translations and transformations to these graphics.

Currently, the disassembler is almost complete and I am working on the "Processor" (the actual execution logic that each instruction performs).




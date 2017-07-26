# NeonObf
NeonObf-uscator is start-up obfuscator

# Base libraries
P.S.: that's needed libraries that must be there are.

https://yadi.sk/d/cdeIXPUM3G5uda

## Usage
java -jar NeonObf.jar <jar_to_obfuscate> <jar_to_obfuscate_out> </path/to/libs/> <transformers> <min/norm/max>

Example: java -jar NeonObf.jar IN.jar OUT.jar libs SourceFileRemover;LineNumberObfuscation;FinalRemover;LocalVariableNameObfuscator;BasicTypesEncryption;GotoFloodObfuscation;CodeHider max

It's highly recommended to use ProGuard with short names obfuscation before NeonObf (ASM are buggy and eats a lot of memory)

## License
Apache =_=

## Communications with author
Skype: moofMonkey

VK: https://new.vk.com/moofmonkey.java

## Needed APIs
ObjectWeb ASM

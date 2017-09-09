# NeonObf
NeonObf-uscator is start-up obfuscator

## Usage
**WARNING**: currently doesn't working with Java 9.

Usage: java -jar NeonObf.jar -d <1/2/3> -i <path> [-l <path>] -o <path> -t <transformers>
 -d,--dictionary <1/2/3>            Dictionary type
 -i,--input <path>                  Input .jar/.class file/folder path
 -l,--libraries <path>              Libraries path (separated by semicolons/multiple arguments)
 -o,--output <path>                 Output .jar path
 -t,--transformers <transformers>   Transformers (separated by semicolons/multiple arguments)

Example: java -jar NeonObf.jar --input IN.jar --output OUT.jar --transformers SourceFileRemover;LineNumberObfuscation;FinalRemover;LocalVariableNameObfuscator;BasicTypesEncryption;GotoFloodObfuscation;CodeHider --dictionary 3

It's highly recommended to use ProGuard with short names obfuscation before NeonObf as it's reducing memory usage of NeonObf

## License
MIT

## Communications with author
Skype: moofMonkey

Discord: moofMonkey#9729

VK: https://new.vk.com/moofmonkey.java

## Needed APIs
ObjectWeb ASM v6.0 (BETA):

 - asm-core
 - asm-analysis
 - asm-commons
 - asm-tree
 - commons-cli

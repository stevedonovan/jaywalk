## Jaywalk - Simple Java Command Line Utilities

Steve Donovan, copyright 2011. X11/MIT License.

Creating command-line programs without fuss can be a pain in any language. If parameters are numbers, the numbers must be checked, and if they are files then the files must be appropriately opened. It is essential for any utility to have useful help, which itself can be a chore.

Jaywalk's `Commandlet` is one solution to the problem. We define a class where any public fields are flags, and any public methods are commands. Items can be marked with `Help` attributes, which allows simple help auto-generation:

    @Help("Demonstrating a simple command-line framework")
    public class SimpleCommand extends Commandlet {
        @Help("scale factor")
        public double scale = 1.0;

        @Help("adds two numbers together and scales")
        public double add(double x, double y) {
            return (x+y)*scale;
        }

        public static void main(String[] args) {
            new SimpleCommand().go(args);
        }

    }

Invocation of this program looks like this:

    $> java SimpleCommand --help
    Demonstrating a simple command-line framework

    Flags:
    -scale: scale factor

    Commands:
    add:    adds two numbers together and scales
    $> java SimpleCommand add 1.2 4.2
    5.4
    $> java SimpleCommand -scale 2 add 1.2 4.2
    10.8

Of course, there is nothing new under the Sun, and I subsequently discovered a little framework called [Cliche](http://code.google.com/p/cliche/wiki/Manual) which does something very similar, except for interactive sessions. This seemed like a good idea, so I taught `Commandlet` an interactive mode:

$> java SimpleCommand -i
? mul 20 43
860.0
? add 2 3
5.0
? add -scale 2 2 3
10.0
?

`Commandlet` knows about input and output text files, which is useful for people like me who can never remember how to open text files in Java:

    @Help("read a file and trim each line")
    public void read(BufferedReader file) throws IOException {
        String line = file.readLine();
        while (line != null) {
            System.out.println(line.trim());
            line = file.readLine();
        }
    }

Parameters of type `int`, `double`, `String`, `BufferedReader` and `PrintStream` are known, and others can be added. Say I have this method:

    public void dec (byte[] arr) {
        for (byte b : arr) {
            System.out.print(b+" ");
        }
        System.out.println();
    }

Then defining how `byte[]` is to be read in can be done like so:

    @Converter
    public byte[] toByteArray(String s) {
        byte[] res = new byte[s.length()/2];
        for (int i = 0, j = 0; i < s.length(); i += 2, j++) {
            String hex = s.substring(i,i+2);
            res[j] = (byte)Short.parseShort(hex, 16);
        }
        return res;
    }

    $> java bytearr dec AF03EE
    -81 3 -18

The strategy is simple: if a parameter type is unknown, then look at all public methods marked with `Converter` and match against their return types. It's interesting to contrast this convention with the approach to [converters](http://code.google.com/p/cliche/wiki/Manual#Converters) taken by Cliche which is more of a classic Java solution (define a new interface and create an anonymous class implementing it).

Simularly, you gain control over output by defining 'stringifiers'

    public byte[] test() { return new byte[] {0x4E,0x3C,0x02}; }

    @Stringifier
    public String asString(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%02X",arr[i]));
        }
        return sb.toString();
    }
    ...
    $> java bytearr test
    4E3C02

Here it's the first argument type that must match the output type, and the return value must be a string. As with Converters, the actual name of the method is not important.


package moh.gov.zm.lis.print;

import moh.gov.zm.lis.config.PrinterProperties;
import moh.gov.zm.lis.exception.ValidationException;
import moh.gov.zm.lis.print.util.ZplLabelEncoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZplLabelEncoderTest {

    // host, port, timeout, strips, pw, ll, module, height, transport, usbSerial
    private final PrinterProperties props = new PrinterProperties("", 9100, 5000, 3, 406, 203, 2, 80, "usb", "");
    private final ZplLabelEncoder encoder = new ZplLabelEncoder(props);

    private static int count(String haystack, String needle) {
        int n = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            n++;
        }
        return n;
    }

    @Test
    void encodesOrderIdAsCode128WithGeometryFromProperties() {
        String zpl = encoder.encode("ORD-1001", 1);

        assertThat(zpl).startsWith("^XA").contains("^XZ");
        assertThat(zpl).contains("^BCN,80,Y,N,N");   // Code 128, height 80, human-readable line
        assertThat(zpl).contains("^BY2");             // module width from props
        assertThat(zpl).contains("^PW406").contains("^LL203");
        assertThat(zpl).contains("^FDORD-1001^FS");   // barcode field data
        assertThat(zpl).contains("^CI28");            // UTF-8
    }

    @Test
    void emitsOneLabelFormatPerStripEachNumbered() {
        String zpl = encoder.encode("ORD-1001", 3);

        assertThat(count(zpl, "^XA")).isEqualTo(3);
        assertThat(count(zpl, "^XZ")).isEqualTo(3);
        assertThat(count(zpl, "^FDORD-1001^FS")).isEqualTo(3);
        assertThat(zpl).contains("1 of 3").contains("2 of 3").contains("3 of 3");
    }

    @Test
    void nonPositiveStripCountFallsBackToOneLabel() {
        assertThat(count(encoder.encode("ORD-1001", 0), "^XA")).isEqualTo(1);
        assertThat(count(encoder.encode("ORD-1001", -5), "^XA")).isEqualTo(1);
    }

    @Test
    void rejectsOrderIdWithZplControlCharacters() {
        assertThatThrownBy(() -> encoder.encode("ORD^1001~x", 3))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsNullOrEmptyOrderId() {
        assertThatThrownBy(() -> encoder.encode(null, 1)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> encoder.encode("", 1)).isInstanceOf(ValidationException.class);
    }
}

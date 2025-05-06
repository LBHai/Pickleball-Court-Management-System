package Data.Model;

public class QRPaymentRemaining {
    private String qrcode;

    public QRPaymentRemaining(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    @Override
    public String toString() {
        return "QRPaymentRemaining{qrcode='" + qrcode + "'}";
    }
}

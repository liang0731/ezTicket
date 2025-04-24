package ecpay.logistics.integration;

import ecpay.logistics.integration.ecpayOperator.EcpayFunction;
import ecpay.logistics.integration.errorMsg.ErrorMessage;
import ecpay.logistics.integration.exception.EcpayException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;

public class AllInOneBase {
    protected static String operatingMode;
    protected static String mercProfile;
    protected static String isProjectContractor;
    protected static String HashKey;
    protected static String HashIV;
    protected static String MerchantID;
    protected static String PlatformID;
    protected static String createUrl;
    protected static String mapUrl;
    protected static String queryLogisticsTradeInfoUrl;
    protected static String logisticsCheckAccountsUrl;
    protected static String updateShipmentInfoUrl;
    protected static String printTradeDocumentUrl;
    protected static String createTestDataUrl;
    protected static String returnHomeUrl;
    protected static String returnCVSUrl;
    protected static String returnHiLifeCVSUrl;
    protected static String returnUniMartCVSUrl;
    protected static String updateStoreInfoUrl;
    protected static String cancelC2COrderUrl;
    protected static String printUniMartC2COrderInfoUrl;
    protected static String printFAMIC2COrderInfoUrl;
    protected static String printHILIFEC2COrderInfoUrl;
    protected static String printOKMARTC2COrderInfoUrl;
    protected static Document verifyDoc;

    public AllInOneBase() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("logistics_conf.xml");
        if (is == null) {
            System.out.println("找不到logistics_conf.xml");
            return;
        }
        Document doc = EcpayFunction.xmlParser(is);

        doc.getDocumentElement().normalize();
        // OperatingMode
        Element ele = (Element) doc.getElementsByTagName("OperatingMode").item(0);
        operatingMode = ele.getTextContent();
        // MercProfile
        ele = (Element) doc.getElementsByTagName("MercProfile").item(0);
        mercProfile = ele.getTextContent();
        // IsProjectContractor
        ele = (Element) doc.getElementsByTagName("IsProjectContractor").item(0);
        isProjectContractor = ele.getTextContent();
        // MID, HashKey, HashIV, PlatformID
        NodeList nodeList = doc.getElementsByTagName("MInfo");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ele = (Element) nodeList.item(i);
            if (ele.getAttribute("name").equalsIgnoreCase(mercProfile)) {
                MerchantID = ele.getElementsByTagName("MerchantID").item(0).getTextContent();
                HashKey = ele.getElementsByTagName("HashKey").item(0).getTextContent();
                HashIV = ele.getElementsByTagName("HashIV").item(0).getTextContent();
                PlatformID = isProjectContractor.equalsIgnoreCase("N") ? "" : MerchantID;
            }
        }
        if (HashKey == null) {
            throw new EcpayException(ErrorMessage.MInfo_NOT_SETTING);
        }
    }
}

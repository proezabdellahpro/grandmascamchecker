package com.grandmaguardian.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    LinearLayout root, resultBox;
    Spinner language;
    EditText message;
    TextView risk, score, explanation, detected, reasons;
    Button check;

    final String[] langs = {"🇬🇧 English", "🇫🇷 Français", "🇸🇦 العربية", "🇲🇦 Darija"};
    final Map<String, String[]> patterns = new LinkedHashMap<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildPatterns();
        buildUI();
    }

    TextView tv(String text, float size) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        v.setPadding(10, 10, 10, 10);
        return v;
    }

    Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(17);
        b.setAllCaps(false);
        return b;
    }

    String tr(String en, String fr, String ar, String da, int lang) {
        return lang == 0 ? en : lang == 1 ? fr : lang == 2 ? ar : da;
    }

    void buildUI() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 30);
        root.setBackgroundColor(Color.rgb(16,16,16));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(16,16,16));
        LinearLayout content = root;

        TextView title = tv("🛡️ Grandma Guardian", 29);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView subtitle = tv("Offline scam-message checker", 15);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        language = new Spinner(this);
        language.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, langs));
        content.addView(language, new LinearLayout.LayoutParams(-1, 58));

        message = new EditText(this);
        message.setTextSize(18);
        message.setGravity(Gravity.TOP | Gravity.START);
        message.setMinHeight(250);
        message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        message.setBackgroundColor(Color.WHITE);
        message.setTextColor(Color.BLACK);
        message.setHintTextColor(Color.GRAY);
        content.addView(message);

        check = button("🔎 Check message");
        content.addView(check);

        resultBox = new LinearLayout(this);
        resultBox.setOrientation(LinearLayout.VERTICAL);
        resultBox.setPadding(16,16,16,16);
        resultBox.setBackgroundColor(Color.rgb(34,34,34));

        risk = tv("Low risk", 26);
        risk.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        score = tv("Risk score: 0/100", 18);
        explanation = tv("", 16);
        detected = tv("", 16);
        reasons = tv("", 16);
        resultBox.addView(risk);
        resultBox.addView(score);
        resultBox.addView(explanation);
        resultBox.addView(detected);
        resultBox.addView(reasons);
        content.addView(resultBox);

        TextView note = tv("Grandma Guardian • offline analysis • no message is uploaded", 12);
        note.setGravity(Gravity.CENTER);
        content.addView(note);

        check.setOnClickListener(v -> analyze());
        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                updateLanguage(pos);
                if (pos >= 2) root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                else root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        });
        scroll.addView(content);
        setContentView(scroll);
        updateLanguage(0);
    }

    void updateLanguage(int lang) {
        message.setHint(tr("Paste a message you received here...",
                "Collez ici un message reçu...", "ألصق الرسالة التي وصلتك هنا...", "لسق هنا الميساج اللي وصلك...", lang));
        check.setText(tr("🔎 Check message", "🔎 Analyser le message", "🔎 فحص الرسالة", "🔎 فحص الميساج", lang));
        risk.setText(tr("Not checked yet", "Pas encore analysé", "لم يتم الفحص بعد", "مازال ما تفحصش", lang));
        score.setText(tr("Risk score: 0/100", "Points de risque : 0/100", "نقط الخطر: 0/100", "نقط الخطر: 0/100", lang));
        explanation.setText("");
        detected.setText("");
        reasons.setText(tr("Tip: never share passwords or one-time codes.", 
                "Conseil : ne partagez jamais un mot de passe ou un code à usage unique.",
                "نصيحة: لا تشارك كلمة السر أو رمز التحقق لمرة واحدة.",
                "نصيحة: ما تعطيش كلمة السر ولا كود التحقق لشي واحد.", lang));
    }

    String normalize(String s) {
        s = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFKC);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        s = s.replace('أ','ا').replace('إ','ا').replace('آ','ا')
             .replace('ى','ي').replace('ئ','ي').replace('ؤ','و').replace('ة','ه').replace('ـ',' ');
        s = s.replaceAll("[^\\p{L}\\p{N}]+", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    String normalizeArabizi(String s) {
        return normalize(s).replace("3","ع").replace("7","ح").replace("9","ق")
                .replace("5","خ").replace("6","ط").replace("8","غ").replace("2","ء");
    }

    boolean has(String text, String... terms) {
        String n = " " + normalize(text) + " ";
        String arabizi = " " + normalizeArabizi(text) + " ";
        for (String term : terms) {
            String p = normalize(term);
            if (p.isEmpty()) continue;
            if (n.contains(" " + p + " ") || arabizi.contains(" " + normalizeArabizi(term) + " ")) return true;
        }
        return false;
    }

    boolean moneyAmount(String s) {
        return Pattern.compile("(?i)\\b\\d{1,6}\\s*(dh|dhs|mad|dirham|dirhams)\\b").matcher(s).find()
            || Pattern.compile("\\b\\d{1,6}\\s*(درهم|دراهم)\\b").matcher(s).find();
    }

    boolean phone(String s) {
        String c = s.replaceAll("[\\s().-]", "");
        return Pattern.compile("(?:\\+212|00212|0)[5-7]\\d{8}\\b").matcher(c).find();
    }

    boolean url(String s) {
        return Pattern.compile("(?i)(https?://|www\\.|[a-z0-9-]+\\.(com|net|org|ma|xyz)\\b)").matcher(s).find();
    }

    void buildPatterns() {
        // Phrase-based, multilingual indicators. These are clues, not proof by themselves.
        patterns.put("money", new String[]{
            "send money","transfer money","pay now","make a payment","payment required","wire money","pay the fee",
            "argent","envoyez de l'argent","envoie de l'argent","virement","paiement urgent","frais a payer","payer maintenant",
            "فلوس","صيفط الفلوس","حول الفلوس","خلص دابا","sift flous","sift lia flous","seft lia flous","7wel flous","kheless daba"
        });
        patterns.put("account", new String[]{
            "bank account","account details","bank details","banking details","your bank account","your account number","account number","routing number","sort code","iban","swift code","bank login",
            "compte bancaire","conte bancaire","coordonnees bancaires","informations bancaires","releve bancaire","numero de compte","rib","votre compte bancaire","identifiants bancaires",
            "حسابك البنكي","معلومات الحساب","رقم الحساب","المعلومات البنكية","رقم البطاقة البنكية","عطيني الكونط","الكونط البنكي",
            "3tini countek","atini countek","a3tini comptek","3tini conte bancaire","conte bancaire dyalek","compte bancaire dyalek","rib dyalek","rib dyalek","lcompte dyalek","l compte dyalek","numero de compte dyalek"
        });
        patterns.put("send", new String[]{
            "send me","send it to me","transfer to me","forward the code","send your","give me your","tell me your","share your details","send your details","send the details","send me your bank","envoyez moi","envoie moi","donne moi","donnez moi","transferez","envoyez vos coordonnees","envoyez votre rib","donne moi ton rib",
            "صيفط ليا","رسل ليا","حول ليا","عطيني","اعطني","أرسل لي","اعطيني","sift liya","seft liya","3tini","3tini lia","3tini liya","a3tini","atini","3afak 3tini","3fak 3tini","3afak sift lia"
        });
        patterns.put("credential", new String[]{
            "password","passcode","one time password","one-time code","verification code","security code","otp","pin code","card number","cvv","mot de passe","code de verification","code confidentiel","code sms","code recu par sms","identifiant et mot de passe",
            "كلمة السر","كلمة المرور","رمز التحقق","كود التحقق","رمز التأكيد","رقم البطاقة","الرقم السري","كود sms","password dyalek","code dyalek","mot de passe dyalek","code sms"
        });
        patterns.put("prize", new String[]{
            "you have won","you won a prize","claim your prize","claim your reward","free gift card","lottery winner","you are the winner","claim your gift","vous avez gagne","reclamez votre prix","vous avez ete selectionne","cadeau gratuit",
            "ربحت جائزة","ربحتي","مبروك ربحت","ربحت هدية","rbe7ti","rb7ti","rbe7ti prix","rb7ti cadeau"
        });
        patterns.put("urgency", new String[]{
            "act now","within 24 hours","account will be closed","account will be blocked","immediately","urgent action required","verify immediately","final warning","de toute urgence","tout de suite","votre compte sera bloque","votre compte sera suspendu","action immediate",
            "دابا حالا","خاصك دابا","ضروري دابا","حسابك غادي يتسد","daba","dghya","darori daba","7ta lyoum","qbel maytsed comptek"
        });
        patterns.put("secrecy", new String[]{
            "don't tell anyone","do not tell anyone","keep this secret","keep it between us","do not share this","ne le dites a personne","garde le secret","ne dites rien a personne",
            "ما تقول لحتى واحد","خليها سر","matgol lta wa7ed","matgol l7ed","khalliha sir"
        });
        patterns.put("remote", new String[]{
            "install anydesk","install teamviewer","give me remote access","share your screen","remote control","access a distance","prenez le controle","installez anydesk","installez teamviewer",
            "تحكم عن بعد","شارك الشاشة","installe anydesk","installi anydesk"
        });
        patterns.put("linkAction", new String[]{
            "click here to verify","click the link to claim","open this link to unlock","enter your details at","verify your account here","cliquez ici pour verifier","ouvrez ce lien","confirmez votre compte via le lien",
            "اضغط هنا للتحقق","دخل للرابط","dkhol l lien","d5ol l lien","clique sur le lien"
        });
        patterns.put("impersonation", new String[]{
            "i am from your bank","this is your bank","bank security team","government agent","police officer","customer service agent","your bank called","je suis de votre banque","service client de votre banque","conseiller bancaire","support officiel de votre banque",
            "انا من البنك","خدمة الزبناء ديال البنك","موظف البنك","ana mn banka","ana men banka","service client dyal banka"
        });
    }

    String label(String key, int lang) {
        String[][] labels = {
            {"Money/payment request","Demande d'argent/paiement","طلب المال أو الدفع","طلب الفلوس أو الخلاص"},
            {"Bank account details","Coordonnées bancaires","معلومات الحساب البنكي","معلومات الكونط البنكي"},
            {"Request to send something","Demande d'envoi","طلب إرسال شيء","طلب السيفط"},
            {"Password or verification code","Mot de passe ou code","كلمة السر أو رمز التحقق","password ولا كود التحقق"},
            {"Prize or reward claim","Promesse de gain","وعد بجائزة","وعد بربح"},
            {"Urgency or pressure","Urgence ou pression","استعجال أو ضغط","الاستعجال أو الضغط"},
            {"Secrecy request","Demande de secret","طلب السرية","طلب السرية"},
            {"Remote access request","Demande d'accès à distance","طلب التحكم عن بعد","طلب التحكم عن بعد"},
            {"Suspicious link instruction","Instruction liée à un lien","تعليمات حول رابط مشبوه","تعليمات على رابط مشبوه"},
            {"Possible impersonation","Possible usurpation d'identité","احتمال انتحال الهوية","احتمال انتحال الهوية"}
        };
        String[] keys = {"money","account","send","credential","prize","urgency","secrecy","remote","linkAction","impersonation"};
        for (int i=0;i<keys.length;i++) if (keys[i].equals(key)) return labels[i][lang];
        if (key.equals("url")) return tr("Web link found","Lien web détecté","تم العثور على رابط","تلقا رابط",lang);
        if (key.equals("phone")) return tr("Phone number found","Numéro de téléphone détecté","تم العثور على رقم هاتف","تلقا رقم تلفون",lang);
        return key;
    }

    void analyze() {
        String text = message.getText().toString().trim();
        int lang = language.getSelectedItemPosition();
        if (text.isEmpty()) {
            explanation.setText(tr("Paste a message first.", "Collez d'abord un message.", "ألصق رسالة أولا.", "لسق الميساج اللول.", lang));
            return;
        }

        Set<String> found = new LinkedHashSet<>();
        for (String key : patterns.keySet()) if (has(text, patterns.get(key))) found.add(key);
        if (url(text)) found.add("url");
        if (phone(text)) found.add("phone");

        int s = 0;
        String darijaInput = normalizeArabizi(text);
        boolean asksForBankData = (has(text, "conte bancaire", "compte bancaire", "bank account", "bank details", "coordonnees bancaires", "informations bancaires", "rib", "حسابك البنكي", "معلومات الحساب", "رقم الحساب")
                || has(darijaInput, "conte bancaire", "compte bancaire", "rib dyalek", "lcompte dyalek"));
        boolean directRequest = has(text, "3tini", "3fak 3tini", "3afak 3tini", "donne moi", "donnez moi", "give me", "send me", "share your", "envoyez moi", "envoie moi", "عطيني", "اعطني", "أرسل لي");
        Map<String,Integer> base = new HashMap<>();
        base.put("money",12); base.put("account",18); base.put("send",10);
        base.put("credential",22); base.put("prize",12); base.put("urgency",12);
        base.put("secrecy",10); base.put("remote",24); base.put("linkAction",14);
        base.put("impersonation",16); base.put("url",5); base.put("phone",3);
        for (String k : found) s += base.getOrDefault(k,0);

        // A request for bank details is high risk even if the wording is misspelled or transliterated.
        if (asksForBankData && directRequest) {
            s += 58;
            found.add("account");
            found.add("send");
        } else if (asksForBankData) {
            s += 24;
            found.add("account");
        }

        // Stronger evidence comes from combinations, not ordinary words in isolation.
        if (found.contains("account") && found.contains("send")) s += 22;
        if (found.contains("account") && found.contains("credential")) s += 25;
        if (found.contains("money") && found.contains("urgency")) s += 18;
        if (found.contains("prize") && found.contains("credential")) s += 24;
        if (found.contains("impersonation") && (found.contains("account") || found.contains("credential"))) s += 24;
        if (found.contains("remote") && (found.contains("credential") || found.contains("account"))) s += 28;
        if (found.contains("linkAction") && (found.contains("credential") || found.contains("account"))) s += 20;
        if (found.contains("secrecy") && (found.contains("money") || found.contains("credential"))) s += 16;
        if (found.contains("url") && (found.contains("credential") || found.contains("linkAction"))) s += 12;
        s = Math.min(s, 100);

        String level = s >= 75 ? "high" : s >= 45 ? "suspicious" : s >= 20 ? "caution" : "low";
        int ri = level.equals("low") ? 0 : level.equals("caution") ? 1 : level.equals("suspicious") ? 2 : 3;
        String[] riskNames = {
            tr("No clear warning signs","Aucun signe évident","لا توجد علامات واضحة","ما بانوش علامات واضحة",lang),
            tr("Be careful","Soyez prudent","انتبه","رد بالك",lang),
            tr("Suspicious message","Message suspect","رسالة مشبوهة","ميساج مشبوه",lang),
            tr("High risk — verify first","Risque élevé — vérifiez d'abord","خطر مرتفع — تحقق أولا","خطر كبير — تأكد اللول",lang)
        };
        risk.setText(riskNames[ri]);
        score.setText(tr("Risk score: ","Points de risque : ","نقط الخطر: ","نقط الخطر: ",lang) + s + "/100");

        StringBuilder d = new StringBuilder();
        d.append(tr("Signals found:\n","Signaux détectés :\n","العلامات المكتشفة:\n","العلامات اللي تلقات:\n",lang));
        if (found.isEmpty()) d.append(tr("No obvious suspicious pattern found. This does not prove the message is safe.",
                "Aucun signal suspect évident. Cela ne prouve pas que le message est sûr.",
                "لم يتم العثور على نمط مشبوه واضح. هذا لا يعني أن الرسالة آمنة.",
                "ما تلقاش نمط مشبوه واضح. هادشي ما كيضمنش أن الميساج آمن.",lang));
        else for (String k : found) d.append("• ").append(label(k,lang)).append("\n");
        detected.setText(d.toString());

        String exp;
        if (s >= 75) exp = tr("⚠️ Do not follow the instructions until you verify the sender using an official contact method.",
                "⚠️ Ne suivez pas les instructions avant d'avoir vérifié l'expéditeur par un moyen officiel.",
                "⚠️ لا تتبع التعليمات قبل التحقق من المرسل عبر وسيلة رسمية.",
                "⚠️ ما تتبعش التعليمات حتى تتأكد من المرسل بطريقة رسمية.",lang);
        else if (s >= 45) exp = tr("⚠️ Several warning signs appear together. Verify independently.",
                "⚠️ Plusieurs signes d'alerte apparaissent ensemble. Vérifiez indépendamment.",
                "⚠️ ظهرت عدة علامات تحذير معا. تحقق بطريقة مستقلة.",
                "⚠️ كاينين عدة علامات تحذير مجموعين. تأكد بوحدك.",lang);
        else if (s >= 20) exp = tr("⚠️ Be careful and check the sender before acting.",
                "⚠️ Soyez prudent et vérifiez l'expéditeur avant d'agir.",
                "⚠️ انتبه وتحقق من المرسل قبل التصرف.",
                "⚠️ رد بالك وتأكد من المرسل قبل ما تدير شي حاجة.",lang);
        else exp = tr("No strong scam pattern was detected. Stay cautious with unexpected requests.",
                "Aucun schéma d'arnaque fort détecté. Restez prudent face aux demandes inattendues.",
                "لم يتم اكتشاف نمط احتيال قوي. كن حذرا من الطلبات غير المتوقعة.",
                "ما تلقاش نمط قوي ديال النصب. رد بالك من الطلبات المفاجئة.",lang);
        explanation.setText(exp);

        reasons.setText(tr("Safety tip: never share passwords, one-time codes, bank details, or remote access. Contact your bank or family through a number you already trust.",
                "Conseil : ne partagez jamais mot de passe, code à usage unique, coordonnées bancaires ou accès à distance. Contactez votre banque ou votre famille via un numéro déjà fiable.",
                "نصيحة: لا تشارك كلمة السر أو رموز التحقق أو معلومات البنك أو التحكم عن بعد. اتصل بالبنك أو العائلة برقم موثوق لديك.",
                "نصيحة: ما تعطيش password ولا كود التحقق ولا معلومات البنك ولا التحكم عن بعد. عيط للبنك أو العائلة برقم كتثق فيه.",lang));

        int c = s >= 75 ? Color.rgb(255,46,109) : s >= 45 ? Color.rgb(233,87,87) : s >= 20 ? Color.rgb(240,180,67) : Color.rgb(69,217,139);
        risk.setTextColor(c);
    }
}

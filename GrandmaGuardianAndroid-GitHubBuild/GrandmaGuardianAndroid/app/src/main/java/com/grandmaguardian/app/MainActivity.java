package com.grandmaguardian.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;
import java.util.regex.*;
import java.text.Normalizer;

public class MainActivity extends Activity {

    LinearLayout root, resultBox;
    Spinner language;
    EditText message;
    TextView risk, score, explanation, detected, reasons;
    Button check;

    final String[] langs = {"🇫🇷 Français", "🇸🇦 العربية", "🇲🇦 Darija"};

    final Map<String, String[]> patterns = new HashMap<>();

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

        TextView subtitle = tv("Protection contre les arnaques et les demandes suspectes.", 15);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        language = new Spinner(this);
        language.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, langs));
        content.addView(language, new LinearLayout.LayoutParams(-1, 58));

        message = new EditText(this);
        message.setTextSize(18);
        message.setGravity(Gravity.TOP | Gravity.START);
        message.setHint("Collez ici un message reçu...");
        message.setMinHeight(250);
        message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        message.setBackgroundColor(Color.WHITE);
        message.setTextColor(Color.BLACK);
        message.setHintTextColor(Color.GRAY);
        content.addView(message);

        check = button("🔎 Analyser le message");
        content.addView(check);

        resultBox = new LinearLayout(this);
        resultBox.setOrientation(LinearLayout.VERTICAL);
        resultBox.setPadding(16,16,16,16);
        resultBox.setBackgroundColor(Color.rgb(34,34,34));

        risk = tv("Risque faible", 26);
        risk.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        score = tv("Points de risque : 0", 18);
        explanation = tv("", 16);
        detected = tv("", 16);
        reasons = tv("", 16);

        resultBox.addView(risk);
        resultBox.addView(score);
        resultBox.addView(explanation);
        resultBox.addView(detected);
        resultBox.addView(reasons);

        content.addView(resultBox);
        TextView note = tv("Grandma Guardian 1.0 • analyse locale • fonctionne hors ligne", 12);
        note.setGravity(Gravity.CENTER);
        content.addView(note);

        check.setOnClickListener(v -> analyze());

        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                updateLanguage(pos);
            }
        });

        scroll.addView(content);
        setContentView(scroll);
    }

    void updateLanguage(int lang) {
        if (lang == 0) {
            message.setHint("Collez ici un message reçu...");
            check.setText("🔎 Analyser le message");
            root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        } else if (lang == 1) {
            message.setHint("ألصق الرسالة هنا...");
            check.setText("🔎 فحص الرسالة");
            root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            message.setHint("لسق الميساج هنا...");
            check.setText("🔎 فحص الميساج");
            root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    String normalize(String s) {
        s = s.toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        s = s.replace('أ','ا').replace('إ','ا').replace('آ','ا')
             .replace('ى','ي').replace('ئ','ي').replace('ؤ','و').replace('ة','ه');
        s = s.replace('ـ',' ');
        s = s.replace('3','ع').replace('7','ح').replace('9','ق')
             .replace('5','خ').replace('6','ط').replace('8','غ').replace('2','ء');
        s = s.replaceAll("[\\p{Punct}_]+", " ");
        s = s.replaceAll("\\s+", " ").trim();
        s = s.replaceAll("(.)\\1{2,}", "$1$1");
        return s;
    }

    boolean has(String text, String... terms) {
        String n = normalize(text);
        String compact = n.replace(" ","");
        for (String t : terms) {
            String p = normalize(t);
            if (n.contains(p) || compact.contains(p.replace(" ",""))) return true;
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
        patterns.put("money", new String[]{
            "money","cash","payment","send money","give me money","argent",
            "paiement","payer","فلوس","الفلوس","مال","نقود","درهم","دراهم",
            "flous","flouss","flos","flooss"
        });
        patterns.put("account", new String[]{
            "account","your account","bank account","compte","ton compte",
            "mon compte","الحساب","حسابك","حسابك البنكي","كونط","الكونط",
            "countek","contek","comptek","compte","counte","compte dyalek",
            "compte dialek","compte diyalk","3tini countek","atini countek",
            "a3tini countek","3tini comptek","atini comptek"
        });
        patterns.put("send", new String[]{
            "send","send me","transfer","forward","envoyer","envoyez",
            "envoie moi","transfert","virement","ارسلي","ارسل لي","حول لي",
            "صيفط ليا","سيفط ليا","siftli","sift liya","seftli","seft liya",
            "sifetli","sefetli"
        });
        patterns.put("personal", new String[]{
            "password","passcode","pin","otp","code","verification code",
            "security code","login","username","email","identity","cin",
            "card number","bank details","mot de passe","code de verification",
            "كلمة السر","رمز التحقق","كود","المعلومات الشخصية","رقم البطاقة",
            "password dyalek","code dyalek"
        });
        patterns.put("phone", new String[]{
            "phone number","telephone number","mobile number","numero de telephone",
            "numero de tel","رقم الهاتف","نوميرو","رقمك","tiliphon","tilifon",
            "telephone","telephon"
        });
        patterns.put("prize", new String[]{
            "you won","winner","prize","reward","gift","free money","lottery",
            "congratulations","vous avez gagne","cadeau","مبروك","ربحت","جائزة",
            "هدية","ربح","mabrouk","rbe7ti","rb7ti"
        });
        patterns.put("urgency", new String[]{
            "urgent","urgently","right now","immediately","hurry","asap",
            "urgentement","tout de suite","immediatement","vite","عاجل","فورا",
            "دابا","دابا حالا","بسرعة","الان","خاصك دابا","ضروري دابا",
            "daba","dghya","dghia","bser3a","darori"
        });
        patterns.put("secrecy", new String[]{
            "don't tell anyone","do not tell anyone","keep this secret","secret",
            "between us","ne le dis a personne","garde le secret","لا تخبر احدا",
            "لا تقول لحد","خليه سر","خليها سر","بيناتنا","ماتقول لحد",
            "matgolch","matgoulch","mat9olch","sir binatna"
        });
        patterns.put("remote", new String[]{
            "anydesk","teamviewer","remote access","remote control",
            "control your computer","control your phone","screen sharing",
            "share your screen","acces a distance","controle a distance",
            "prendre le controle","تحكم عن بعد","دخول عن بعد","التحكم في الهاتف",
            "التحكم في الحاسوب","شارك الشاشة"
        });
        patterns.put("link", new String[]{
            "http://","https://","www.","click here","click the link","open this link",
            "link","clique ici","ouvre ce lien","lien","اضغط هنا","افتح الرابط",
            "الرابط","لينك"
        });
        patterns.put("meeting", new String[]{
            "meet me","meeting","rendez vous","rejoins moi","viens me voir",
            "تعال عندي","قابلني","اللقاء","موعد","ntla9aw","ntla9o","ntla9ou",
            "tl9ani","tla9ani","ji 3ndi"
        });
        patterns.put("impersonation", new String[]{
            "i am the bank","i am from the bank","bank employee","customer service",
            "support team","police","government","your son","your daughter",
            "your grandson","je suis de la banque","service client","gouvernement",
            "انا من البنك","خدمة الزبناء","الشرطة","الحكومة","ولدك","بنتك","حفيدك",
            "ana men banka","ana mn banka"
        });
    }

    String label(String key, int lang) {
        String[][] x = {
            {"argent / paiement","المال / الدفع","الفلوس / الخلاص"},
            {"compte","الحساب","الكونط / الحساب"},
            {"envoi / transfert","الإرسال / التحويل","السيفط / التحويل"},
            {"information personnelle","معلومات شخصية","معلومات شخصية"},
            {"numéro de téléphone","رقم الهاتف","نوميرو ديال التلفون"},
            {"cadeau / gain","جائزة / ربح","ربح / جائزة"},
            {"urgence / pression","استعجال / ضغط","الاستعجال / الضغط"},
            {"secret","سرية","السرية"},
            {"accès à distance","تحكم عن بعد","التحكم عن بعد"},
            {"lien","رابط","لينك"},
            {"rendez-vous","لقاء","اللقاء"},
            {"usurpation d'identité","انتحال الهوية","انتحال الهوية"}
        };
        String[] keys = {"money","account","send","personal","phone","prize","urgency","secrecy","remote","link","meeting","impersonation"};
        for (int i=0;i<keys.length;i++) if(keys[i].equals(key)) return x[i][lang];
        return key;
    }

    void analyze() {
        String text = message.getText().toString();
        int lang = language.getSelectedItemPosition();

        Set<String> found = new LinkedHashSet<>();
        for (String key : patterns.keySet()) {
            String[] arr = patterns.get(key);
            if (has(text, arr)) found.add(key);
        }

        if (moneyAmount(text)) found.add("money");
        if (phone(text)) found.add("phone");
        if (url(text)) found.add("link");

        int s = 0;
        Map<String,Integer> base = new HashMap<>();
        base.put("money",16); base.put("account",14); base.put("send",14);
        base.put("personal",17); base.put("phone",8); base.put("prize",14);
        base.put("urgency",11); base.put("secrecy",13); base.put("remote",18);
        base.put("link",9); base.put("meeting",8); base.put("impersonation",13);

        for (String k: found) s += base.getOrDefault(k,0);

        if(found.contains("account") && found.contains("send")) s+=25;
        if(found.contains("account") && found.contains("money")) s+=24;
        if(found.contains("account") && found.contains("personal")) s+=24;
        if(found.contains("money") && found.contains("send")) s+=24;
        if(found.contains("money") && found.contains("urgency")) s+=22;
        if(found.contains("money") && found.contains("secrecy")) s+=22;
        if(found.contains("personal") && found.contains("link")) s+=18;
        if(found.contains("personal") && found.contains("impersonation")) s+=22;
        if(found.contains("remote") && found.contains("personal")) s+=28;
        if(found.contains("remote") && found.contains("account")) s+=26;
        if(found.contains("prize") && found.contains("money")) s+=24;
        if(found.contains("secrecy") && found.contains("urgency")) s+=20;

        String n = normalize(text).replace(" ","");
        if (n.contains(normalize("3tini countek").replace(" ","")) ||
            n.contains(normalize("atini countek").replace(" ","")) ||
            n.contains(normalize("a3tini countek").replace(" ",""))) {
            s += 30;
            found.add("account");
        }

        if(found.contains("money") && found.contains("send") && found.contains("account")) s+=20;
        if(found.contains("money") && found.contains("send") && found.contains("personal")) s+=25;
        if(found.size() >= 4) s+=10;

        s = Math.min(s,100);

        String level;
        if(s>=80) level="extreme";
        else if(s>=60) level="high";
        else if(s>=35) level="suspicious";
        else if(s>=15) level="caution";
        else level="low";

        String[] riskFR={"Risque faible","Prudence","Suspect","Risque élevé","Risque très élevé"};
        String[] riskAR={"الخطر ضعيف","انتبه","مشبوه","خطر مرتفع","خطر مرتفع جداً"};
        String[] riskDA={"الخطر ضعيف","رد بالك","مشبوه","خطر كبير","خطر كبير بزاف"};
        int ri = level.equals("low")?0:level.equals("caution")?1:level.equals("suspicious")?2:level.equals("high")?3:4;

        String rn = lang==0?riskFR[ri]:lang==1?riskAR[ri]:riskDA[ri];
        risk.setText(rn);
        score.setText((lang==0?"Points de risque : ":lang==1?"نقط الخطر: ":"نقط الخطر: ")+s);

        StringBuilder d = new StringBuilder();
        d.append(lang==0?"Éléments détectés :\n":lang==1?"العناصر المكتشفة:\n":"الحوايج اللي تلقاو:\n");
        if(found.isEmpty()) {
            d.append(lang==0?"Aucun signal évident.":lang==1?"ما بان حتى دليل واضح.":"ما بان حتى شي دليل واضح.");
        } else {
            for(String k:found) d.append("• ").append(label(k,lang)).append("\n");
        }
        detected.setText(d.toString());

        String exp;
        if(s>=60) exp=lang==0?"⚠️ Ne faites pas confiance à ce message sans vérification.":lang==1?"⚠️ لا تثق في هذه الرسالة قبل التحقق منها.":"⚠️ ما تثقش فهاد الميساج حتى تتأكد.";
        else if(s>=35) exp=lang==0?"⚠️ Ce message présente des signes suspects.":lang==1?"⚠️ هذه الرسالة فيها علامات مشبوهة.":"⚠️ هاد الميساج فيه علامات مشبوهة.";
        else if(s>=15) exp=lang==0?"⚠️ Soyez prudent.":lang==1?"⚠️ رد بالك.":"⚠️ رد بالك.";
        else exp=lang==0?"Aucun signe évident d'arnaque détecté.":lang==1?"لم يتم العثور على علامة واضحة للاحتيال.":"ما بان حتى شي دليل واضح ديال النصب.";
        explanation.setText(exp);

        StringBuilder r = new StringBuilder();
        r.append(lang==0?"Conseil : ne partagez jamais un mot de passe, code OTP ou accès à distance avec quelqu'un qui vous le demande.":
                 lang==1?"نصيحة: لا تشارك أبداً كلمة السر أو رمز OTP أو التحكم عن بعد مع شخص يطلبها منك.":
                 "نصيحة: عمرَك تعطي password ولا code OTP ولا accès للتلفون لشي واحد كيطلبهم منك.");
        reasons.setText(r.toString());

        int c = s>=80?Color.rgb(255,46,109):s>=60?Color.rgb(233,87,87):s>=35?Color.rgb(240,154,67):s>=15?Color.rgb(240,200,75):Color.rgb(69,217,139);
        risk.setTextColor(c);
    }
}

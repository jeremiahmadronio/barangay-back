package com.barangay.barangay.blotter.constant;

import java.util.List;

public class EvidenceConstants {
    public static final List<String> VALID_EVIDENCE_NAMES = List.of(
            // 1. Medical
            "Medical Certificate", "Medico-Legal Report", "Psychological Evaluation Report", "Dental Record", "Lab Result (Toxicology/X-Ray/etc.)",
            // 2. Testimonial
            "Affidavit / Sworn Statement", "Complaint Affidavit", "Witness Statement", "Kagawad/Lupon Mediation Agreement", "Police Blotter/Report Referral", "Court Order / Subpoena",
            // 3. Digital
            "Screenshot (Messenger/Viber/WhatsApp)", "Screenshot (Facebook/Post/Comment)", "Voice/Audio Recording", "Email Print-out", "SMS / Text Message Transcript",
            // 4. Visual
            "Photograph (Incident Scene)", "Photograph (Physical Injury)", "Photograph (Property Damage)", "CCTV Footage (Digital/USB)", "Dashcam Footage", "Hand-drawn Map or Sketch",
            // 5. Material
            "Physical Item (Weapon/Tool/etc.)", "Clothing or Personal Belonging", "Illegal Substance (Turned over to Police)", "Actual Damaged Equipment/Part",
            // 6. Financial
            "Official Receipt / Proof of Payment", "Contract / Lease Agreement", "Land Title / Deed of Sale",
            // 7. Others
            "Other Supporting Documents/Items"
    );
}
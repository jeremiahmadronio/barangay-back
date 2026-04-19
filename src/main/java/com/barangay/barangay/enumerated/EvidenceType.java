package com.barangay.barangay.enumerated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EvidenceType {

    // 1. MEDICAL & FORENSIC (Highly Sensitive)
    MEDICAL_CERTIFICATE("Medical Certificate"),
    MEDICO_LEGAL_REPORT("Medico-Legal Report"),
    PSYCHOLOGICAL_EVALUATION("Psychological Evaluation Report"),
    DENTAL_RECORD("Dental Record"),
    LABORATORY_RESULT("Lab Result (Toxicology/X-Ray/etc.)"),

    // 2. TESTIMONIAL & LEGAL DOCUMENTS
    AFFIDAVIT("Affidavit / Sworn Statement"),
    COMPLAINT_AFFIDAVIT("Complaint Affidavit"),
    WITNESS_STATEMENT("Witness Statement"),
    MEDIATION_AGREEMENT("Kagawad/Lupon Mediation Agreement"),
    POLICE_REPORT("Police Blotter/Report Referral"),
    COURT_ORDER("Court Order / Subpoena"),

    // 3. DIGITAL & ELECTRONIC EVIDENCE
    SCREENSHOT_CHAT("Screenshot (Messenger/Viber/WhatsApp)"),
    SCREENSHOT_SOCIAL_MEDIA("Screenshot (Facebook/Post/Comment)"),
    AUDIO_RECORDING("Voice/Audio Recording"),
    EMAIL_PRINT_OUT("Email Print-out"),
    SMS_TRANSCRIPT("SMS / Text Message Transcript"),

    // 4. VISUAL & MULTIMEDIA
    PHOTOGRAPH_SCENE("Photograph (Incident Scene)"),
    PHOTOGRAPH_INJURY("Photograph (Physical Injury)"),
    PHOTOGRAPH_PROPERTY("Photograph (Property Damage)"),
    CCTV_FOOTAGE("CCTV Footage (Digital/USB)"),
    DASHCAM_VIDEO("Dashcam Footage"),
    MAP_SKETCH("Hand-drawn Map or Sketch"),

    // 5. MATERIAL & PHYSICAL OBJECTS
    PHYSICAL_OBJECT("Physical Item (Weapon/Tool/etc.)"),
    CLOTHING_ITEM("Clothing or Personal Belonging"),
    ILLEGAL_SUBSTANCE_REPLICA("Illegal Substance (Turned over to Police)"),
    DAMAGED_EQUIPMENT("Actual Damaged Equipment/Part"),

    // 6. FINANCIAL & ADMINISTRATIVE
    OFFICIAL_RECEIPT("Official Receipt / Proof of Payment"),
    CONTRACT_AGREEMENT("Contract / Lease Agreement"),
    LAND_TITLE_DEED("Land Title / Deed of Sale"),

    // 7. OTHERS
    OTHERS("Other Supporting Documents/Items");

    private final String label;
}
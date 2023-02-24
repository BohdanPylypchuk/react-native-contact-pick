import Foundation
import ContactsUI

@objc(ContactPick)
class ContactPick: NSObject, CNContactPickerDelegate {
  static let MODULE_NAME = "ContactPick"
  private let ERR_CODE_CANCELED = "ERR_CANCELLED"
    
  var promise:(resolve: RCTPromiseResolveBlock?, reject: RCTPromiseRejectBlock?) = (resolve: nil, reject: nil);
  var isPhoneNumberDigital: Bool = false

  static func moduleName() -> String! {
    return MODULE_NAME
  }
    
  @objc
  func constantsToExport() -> [String: Any]! {
    return [
      "ERR_CODE_CANCELED": ERR_CODE_CANCELED
    ]
  }
    
  static var requiresMainQueueSetup: Bool {
    return true
  }


  @objc func pickContact(_ options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    self.promise.resolve = resolve
    self.promise.reject = reject
    self.isPhoneNumberDigital = options["isPhoneNumberDigital"] as? Bool ?? false
    presentContactPicker()
  }
  
  private func presentContactPicker() {
    let contactPicker = CNContactPickerViewController()
    contactPicker.delegate = self
    
    DispatchQueue.main.async {
      let rootViewController = UIApplication.shared.keyWindow?.rootViewController
      rootViewController?.present(contactPicker, animated: true, completion: nil)
    }
  }
  
  func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {
    var contactInfo: [String: Any] = [:]
      
    let fullName = CNContactFormatter.string(from: contact, style: .fullName)
    contactInfo["fullName"] = fullName ?? ""
      
    let phoneNumbers = getPhoneNumbers(from: contact)
    contactInfo["phoneNumbers"] = phoneNumbers.count > 1 ? phoneNumbers : NSNull()
  
    let emails = getEmails(from: contact)
    contactInfo["emails"] = emails.count > 1 ? emails : NSNull()
      
      
    self.promise.resolve!(contactInfo)
  }
  
  private func getPhoneNumbers(from contact: CNContact) -> [[String: String]] {
    var phoneNumbers: [[String: String]] = []
    for phoneNumber in contact.phoneNumbers {
      let label = CNLabeledValue<NSString>.localizedString(forLabel: phoneNumber.label ?? "")
      let phoneNumberValue = self.isPhoneNumberDigital ? (phoneNumber.value.value(forKey: "digits")) as! String : phoneNumber.value.stringValue
      let phoneInfo = ["type": label, "number": phoneNumberValue]
      phoneNumbers.append(phoneInfo)
    }

    return phoneNumbers
  }
  
  private func getEmails(from contact: CNContact) -> [[String: String]] {
    var emails: [[String: String]] = []
      for email in contact.emailAddresses {
      let label = CNLabeledValue<NSString>.localizedString(forLabel: email.label ?? "")
      let emailValue = email.value as String
      let emailInfo = ["type": label, "email": emailValue]
      emails.append(emailInfo)
    }
    
    return emails
  }

  func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
    self.promise.reject?(ERR_CODE_CANCELED, "The user cancelled the contact picker", nil)
  }
}

export interface PickContactOptions {
  isPhoneNumberDigital?: boolean;
}

export interface PickContactResult {
  fullName: string;
  phoneNumbers:
    | {
        type: string;
        number: string;
      }[]
    | null;
  emails:
    | {
        type: string;
        email: string;
      }[]
    | null;
}

export interface PickContactErrors {
  ERR_CODE_CANCELED: 'ERR_CODE_CANCELED';

  // Android Errors
  ERR_CODE_PERMISSION_NOT_GRANTED?: 'ERR_CODE_PERMISSION_NOT_GRANTED';
}

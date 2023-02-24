import { NativeModules, Platform } from 'react-native';
import type { PickContactErrors, PickContactOptions, PickContactResult } from './types';

const LINKING_ERROR =
  `The package 'react-native-contact-pick' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ContactPick = NativeModules.ContactPick
  ? NativeModules.ContactPick
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

    export const ERROR_CODES: PickContactErrors = ContactPick.getConstants();

    export function pickContact(
      options: PickContactOptions = {}
    ): Promise<PickContactResult> {
      return ContactPick.pickContact(options);
    }
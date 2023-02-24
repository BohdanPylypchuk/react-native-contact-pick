# react-native-contact-pick

react-native-contact-pick is a simple and easy-to-use library that provides a way to access the device's contacts and allows users to select one or more contacts from their address book.

#### Installation

```sh
npm install react-native-contact-pick
```

Make sure your manifest files includes permission to read contacts

```
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

#### Usage

Make sure you ask the `READ_CONTACTS` permission on android.

```js
import { pickContact } from 'react-native-contact-pick';

// ...

const onPress = async () => {
  try {
    const granted =
      Platform.OS === 'android'
        ? (await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.READ_CONTACTS
          )) === PermissionsAndroid.RESULTS.GRANTED
        : true;
    if (granted) {
      const res = await pickContact();
      // do some stuff
    }
  } catch (error) {
    if (error.code !== ERROR_CODES.ERR_CODE_CANCELED) {
      // error
    }
  }
};
```

#### Request Object

| Property             |  Type   | Description                                                                      |
| :------------------- | :-----: | :------------------------------------------------------------------------------- |
| isPhoneNumberDigital | boolean | Convert phones number to digital.<br>`false` : 123-43-56-78<br>`true`: 123435678 |

### Contact Result Object Type

```ts
interface PickContactResult {
  fullName: string;
  phoneNumbers:
    | {
        type: string;
        number: string;
      }[]
    | null; // contact doesn't have phone numbers
  emails:
    | {
        type: string;
        email: string;
      }[]
    | null; // contact doesn't have emails
}
```

#### Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

#### License

MIT

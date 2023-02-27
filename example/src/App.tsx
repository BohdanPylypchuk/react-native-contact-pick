import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  PermissionsAndroid,
  Platform,
} from 'react-native';
import { ERROR_CODES, pickContact } from 'react-native-contact-pick';

export default function App() {
  const onPress = React.useCallback(async () => {
    try {
      const granted =
        Platform.OS === 'android'
          ? (await PermissionsAndroid.request(
              PermissionsAndroid.PERMISSIONS.READ_CONTACTS
            )) === PermissionsAndroid.RESULTS.GRANTED
          : true;
      if (granted) {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const res = await pickContact();
        // do some stuff
      }
    } catch (error) {
      if (error.code === ERROR_CODES.ERR_CODE_CANCELED) {
        // canceled
      }
    }
  }, []);

  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={onPress}>
        <Text>Select contact</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});

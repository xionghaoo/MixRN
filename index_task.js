import React from 'react';
import {AppRegistry, StyleSheet, Text, View, Image} from 'react-native';

class HelloWorld extends React.Component {
  render() {
    let pic = {
      uri: 'https://upload.wikimedia.org/wikipedia/commons/d/de/Bananavarieties.jpg'
    }
    return (
      <View>
        <Text>This is Task RN Page</Text>
        <Text>This is second RN Page</Text>
      </View>
    );
  }
}
var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('TaskNativeApp', () => HelloWorld);
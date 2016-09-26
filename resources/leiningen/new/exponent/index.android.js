var modules={'react-native': require('react-native'), 'react': require('react')};modules['exponent']=require('exponent');modules['@exponent/ex-navigation']=require('@exponent/ex-navigation');
require('figwheel-bridge').withModules(modules).start('main','android','localhost');

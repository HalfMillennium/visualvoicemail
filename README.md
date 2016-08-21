# VisualVoicemail for Android

VisualVoicemail connects to standard IMAP-based VisualVoicemail servers (popularized by iPhone) that are deployed by many carriers across the world.

It is a fork of the awesome open-source K-9 email client for Android which you can find on the [play store](https://play.google.com/store/apps/details?id=com.fsck.k9) and [github](https://github.com/k9mail/k-9).

VisualVoicemail is defined by the [OMTP standard](http://www.gsma.com/newsroom/wp-content/uploads/2012/07/OMTP_VVM_Specification_1_3.pdf) and relies on both SMS and IMAP protocols to deliver the service.

# Limitation

Due to the inability to receive type0 SMS messages without being root or a system app, the voicemail server settings must be known in order to configure vvm.

Current known configurations:
- Vodafone AU
- Telstra (password is a random cram_md5 sent in a type0 SMS, unlike Vodafone that is simply the users voicemail PIN).

NOTE: This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. The software or developers are not affiliated with any carriers or service providers mentioned in this software or attached documentation.


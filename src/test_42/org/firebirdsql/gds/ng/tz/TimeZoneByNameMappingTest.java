/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.tz;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TimeZoneByNameMappingTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final TimeZoneMapping mapping = TimeZoneMapping.getInstance();

    private final int firebirdZoneId;
    private final String zoneName;
    private final String jtZoneId;

    public TimeZoneByNameMappingTest(int firebirdZoneId, String zoneName, String jtZoneId) {
        this.firebirdZoneId = firebirdZoneId;
        this.zoneName = zoneName;
        this.jtZoneId = jtZoneId != null ? jtZoneId : zoneName;
    }

    @Test
    public void mapsToJavaTimeZoneId() {
        ZoneId zoneId = mapping.timeZoneById(firebirdZoneId);

        assertEquals(zoneName + ": " + zoneId, jtZoneId, zoneId.getId());
    }

    @Parameterized.Parameters(name = "{0} => {1})")
    public static Collection<Object[]> getTestCases() {
        return Arrays.asList(
                testCase(65535, "GMT"),
                testCase(65534, "ACT", "Australia/Darwin"),
                testCase(65533, "AET", "Australia/Sydney"),
                testCase(65532, "AGT", "America/Argentina/Buenos_Aires"),
                testCase(65531, "ART", "Africa/Cairo"),
                testCase(65530, "AST", "America/Anchorage"),
                testCase(65529, "Africa/Abidjan"),
                testCase(65528, "Africa/Accra"),
                testCase(65527, "Africa/Addis_Ababa"),
                testCase(65526, "Africa/Algiers"),
                testCase(65525, "Africa/Asmara"),
                testCase(65524, "Africa/Asmera"),
                testCase(65523, "Africa/Bamako"),
                testCase(65522, "Africa/Bangui"),
                testCase(65521, "Africa/Banjul"),
                testCase(65520, "Africa/Bissau"),
                testCase(65519, "Africa/Blantyre"),
                testCase(65518, "Africa/Brazzaville"),
                testCase(65517, "Africa/Bujumbura"),
                testCase(65516, "Africa/Cairo"),
                testCase(65515, "Africa/Casablanca"),
                testCase(65514, "Africa/Ceuta"),
                testCase(65513, "Africa/Conakry"),
                testCase(65512, "Africa/Dakar"),
                testCase(65511, "Africa/Dar_es_Salaam"),
                testCase(65510, "Africa/Djibouti"),
                testCase(65509, "Africa/Douala"),
                testCase(65508, "Africa/El_Aaiun"),
                testCase(65507, "Africa/Freetown"),
                testCase(65506, "Africa/Gaborone"),
                testCase(65505, "Africa/Harare"),
                testCase(65504, "Africa/Johannesburg"),
                testCase(65503, "Africa/Juba"),
                testCase(65502, "Africa/Kampala"),
                testCase(65501, "Africa/Khartoum"),
                testCase(65500, "Africa/Kigali"),
                testCase(65499, "Africa/Kinshasa"),
                testCase(65498, "Africa/Lagos"),
                testCase(65497, "Africa/Libreville"),
                testCase(65496, "Africa/Lome"),
                testCase(65495, "Africa/Luanda"),
                testCase(65494, "Africa/Lubumbashi"),
                testCase(65493, "Africa/Lusaka"),
                testCase(65492, "Africa/Malabo"),
                testCase(65491, "Africa/Maputo"),
                testCase(65490, "Africa/Maseru"),
                testCase(65489, "Africa/Mbabane"),
                testCase(65488, "Africa/Mogadishu"),
                testCase(65487, "Africa/Monrovia"),
                testCase(65486, "Africa/Nairobi"),
                testCase(65485, "Africa/Ndjamena"),
                testCase(65484, "Africa/Niamey"),
                testCase(65483, "Africa/Nouakchott"),
                testCase(65482, "Africa/Ouagadougou"),
                testCase(65481, "Africa/Porto-Novo"),
                testCase(65480, "Africa/Sao_Tome"),
                testCase(65479, "Africa/Timbuktu"),
                testCase(65478, "Africa/Tripoli"),
                testCase(65477, "Africa/Tunis"),
                testCase(65476, "Africa/Windhoek"),
                testCase(65475, "America/Adak"),
                testCase(65474, "America/Anchorage"),
                testCase(65473, "America/Anguilla"),
                testCase(65472, "America/Antigua"),
                testCase(65471, "America/Araguaina"),
                testCase(65470, "America/Argentina/Buenos_Aires"),
                testCase(65469, "America/Argentina/Catamarca"),
                testCase(65468, "America/Argentina/ComodRivadavia"),
                testCase(65467, "America/Argentina/Cordoba"),
                testCase(65466, "America/Argentina/Jujuy"),
                testCase(65465, "America/Argentina/La_Rioja"),
                testCase(65464, "America/Argentina/Mendoza"),
                testCase(65463, "America/Argentina/Rio_Gallegos"),
                testCase(65462, "America/Argentina/Salta"),
                testCase(65461, "America/Argentina/San_Juan"),
                testCase(65460, "America/Argentina/San_Luis"),
                testCase(65459, "America/Argentina/Tucuman"),
                testCase(65458, "America/Argentina/Ushuaia"),
                testCase(65457, "America/Aruba"),
                testCase(65456, "America/Asuncion"),
                testCase(65455, "America/Atikokan"),
                testCase(65454, "America/Atka"),
                testCase(65453, "America/Bahia"),
                testCase(65452, "America/Bahia_Banderas"),
                testCase(65451, "America/Barbados"),
                testCase(65450, "America/Belem"),
                testCase(65449, "America/Belize"),
                testCase(65448, "America/Blanc-Sablon"),
                testCase(65447, "America/Boa_Vista"),
                testCase(65446, "America/Bogota"),
                testCase(65445, "America/Boise"),
                testCase(65444, "America/Buenos_Aires"),
                testCase(65443, "America/Cambridge_Bay"),
                testCase(65442, "America/Campo_Grande"),
                testCase(65441, "America/Cancun"),
                testCase(65440, "America/Caracas"),
                testCase(65439, "America/Catamarca"),
                testCase(65438, "America/Cayenne"),
                testCase(65437, "America/Cayman"),
                testCase(65436, "America/Chicago"),
                testCase(65435, "America/Chihuahua"),
                testCase(65434, "America/Coral_Harbour"),
                testCase(65433, "America/Cordoba"),
                testCase(65432, "America/Costa_Rica"),
                testCase(65431, "America/Creston"),
                testCase(65430, "America/Cuiaba"),
                testCase(65429, "America/Curacao"),
                testCase(65428, "America/Danmarkshavn"),
                testCase(65427, "America/Dawson"),
                testCase(65426, "America/Dawson_Creek"),
                testCase(65425, "America/Denver"),
                testCase(65424, "America/Detroit"),
                testCase(65423, "America/Dominica"),
                testCase(65422, "America/Edmonton"),
                testCase(65421, "America/Eirunepe"),
                testCase(65420, "America/El_Salvador"),
                testCase(65419, "America/Ensenada"),
                testCase(65418, "America/Fort_Nelson"),
                testCase(65417, "America/Fort_Wayne"),
                testCase(65416, "America/Fortaleza"),
                testCase(65415, "America/Glace_Bay"),
                testCase(65414, "America/Godthab"),
                testCase(65413, "America/Goose_Bay"),
                testCase(65412, "America/Grand_Turk"),
                testCase(65411, "America/Grenada"),
                testCase(65410, "America/Guadeloupe"),
                testCase(65409, "America/Guatemala"),
                testCase(65408, "America/Guayaquil"),
                testCase(65407, "America/Guyana"),
                testCase(65406, "America/Halifax"),
                testCase(65405, "America/Havana"),
                testCase(65404, "America/Hermosillo"),
                testCase(65403, "America/Indiana/Indianapolis"),
                testCase(65402, "America/Indiana/Knox"),
                testCase(65401, "America/Indiana/Marengo"),
                testCase(65400, "America/Indiana/Petersburg"),
                testCase(65399, "America/Indiana/Tell_City"),
                testCase(65398, "America/Indiana/Vevay"),
                testCase(65397, "America/Indiana/Vincennes"),
                testCase(65396, "America/Indiana/Winamac"),
                testCase(65395, "America/Indianapolis"),
                testCase(65394, "America/Inuvik"),
                testCase(65393, "America/Iqaluit"),
                testCase(65392, "America/Jamaica"),
                testCase(65391, "America/Jujuy"),
                testCase(65390, "America/Juneau"),
                testCase(65389, "America/Kentucky/Louisville"),
                testCase(65388, "America/Kentucky/Monticello"),
                testCase(65387, "America/Knox_IN"),
                testCase(65386, "America/Kralendijk"),
                testCase(65385, "America/La_Paz"),
                testCase(65384, "America/Lima"),
                testCase(65383, "America/Los_Angeles"),
                testCase(65382, "America/Louisville"),
                testCase(65381, "America/Lower_Princes"),
                testCase(65380, "America/Maceio"),
                testCase(65379, "America/Managua"),
                testCase(65378, "America/Manaus"),
                testCase(65377, "America/Marigot"),
                testCase(65376, "America/Martinique"),
                testCase(65375, "America/Matamoros"),
                testCase(65374, "America/Mazatlan"),
                testCase(65373, "America/Mendoza"),
                testCase(65372, "America/Menominee"),
                testCase(65371, "America/Merida"),
                testCase(65370, "America/Metlakatla"),
                testCase(65369, "America/Mexico_City"),
                testCase(65368, "America/Miquelon"),
                testCase(65367, "America/Moncton"),
                testCase(65366, "America/Monterrey"),
                testCase(65365, "America/Montevideo"),
                testCase(65364, "America/Montreal"),
                testCase(65363, "America/Montserrat"),
                testCase(65362, "America/Nassau"),
                testCase(65361, "America/New_York"),
                testCase(65360, "America/Nipigon"),
                testCase(65359, "America/Nome"),
                testCase(65358, "America/Noronha"),
                testCase(65357, "America/North_Dakota/Beulah"),
                testCase(65356, "America/North_Dakota/Center"),
                testCase(65355, "America/North_Dakota/New_Salem"),
                testCase(65354, "America/Ojinaga"),
                testCase(65353, "America/Panama"),
                testCase(65352, "America/Pangnirtung"),
                testCase(65351, "America/Paramaribo"),
                testCase(65350, "America/Phoenix"),
                testCase(65349, "America/Port-au-Prince"),
                testCase(65348, "America/Port_of_Spain"),
                testCase(65347, "America/Porto_Acre"),
                testCase(65346, "America/Porto_Velho"),
                testCase(65345, "America/Puerto_Rico"),
                testCase(65344, "America/Punta_Arenas"),
                testCase(65343, "America/Rainy_River"),
                testCase(65342, "America/Rankin_Inlet"),
                testCase(65341, "America/Recife"),
                testCase(65340, "America/Regina"),
                testCase(65339, "America/Resolute"),
                testCase(65338, "America/Rio_Branco"),
                testCase(65337, "America/Rosario"),
                testCase(65336, "America/Santa_Isabel"),
                testCase(65335, "America/Santarem"),
                testCase(65334, "America/Santiago"),
                testCase(65333, "America/Santo_Domingo"),
                testCase(65332, "America/Sao_Paulo"),
                testCase(65331, "America/Scoresbysund"),
                testCase(65330, "America/Shiprock"),
                testCase(65329, "America/Sitka"),
                testCase(65328, "America/St_Barthelemy"),
                testCase(65327, "America/St_Johns"),
                testCase(65326, "America/St_Kitts"),
                testCase(65325, "America/St_Lucia"),
                testCase(65324, "America/St_Thomas"),
                testCase(65323, "America/St_Vincent"),
                testCase(65322, "America/Swift_Current"),
                testCase(65321, "America/Tegucigalpa"),
                testCase(65320, "America/Thule"),
                testCase(65319, "America/Thunder_Bay"),
                testCase(65318, "America/Tijuana"),
                testCase(65317, "America/Toronto"),
                testCase(65316, "America/Tortola"),
                testCase(65315, "America/Vancouver"),
                testCase(65314, "America/Virgin"),
                testCase(65313, "America/Whitehorse"),
                testCase(65312, "America/Winnipeg"),
                testCase(65311, "America/Yakutat"),
                testCase(65310, "America/Yellowknife"),
                testCase(65309, "Antarctica/Casey"),
                testCase(65308, "Antarctica/Davis"),
                testCase(65307, "Antarctica/DumontDUrville"),
                testCase(65306, "Antarctica/Macquarie"),
                testCase(65305, "Antarctica/Mawson"),
                testCase(65304, "Antarctica/McMurdo"),
                testCase(65303, "Antarctica/Palmer"),
                testCase(65302, "Antarctica/Rothera"),
                testCase(65301, "Antarctica/South_Pole"),
                testCase(65300, "Antarctica/Syowa"),
                testCase(65299, "Antarctica/Troll"),
                testCase(65298, "Antarctica/Vostok"),
                testCase(65297, "Arctic/Longyearbyen"),
                testCase(65296, "Asia/Aden"),
                testCase(65295, "Asia/Almaty"),
                testCase(65294, "Asia/Amman"),
                testCase(65293, "Asia/Anadyr"),
                testCase(65292, "Asia/Aqtau"),
                testCase(65291, "Asia/Aqtobe"),
                testCase(65290, "Asia/Ashgabat"),
                testCase(65289, "Asia/Ashkhabad"),
                testCase(65288, "Asia/Atyrau"),
                testCase(65287, "Asia/Baghdad"),
                testCase(65286, "Asia/Bahrain"),
                testCase(65285, "Asia/Baku"),
                testCase(65284, "Asia/Bangkok"),
                testCase(65283, "Asia/Barnaul"),
                testCase(65282, "Asia/Beirut"),
                testCase(65281, "Asia/Bishkek"),
                testCase(65280, "Asia/Brunei"),
                testCase(65279, "Asia/Calcutta"),
                testCase(65278, "Asia/Chita"),
                testCase(65277, "Asia/Choibalsan"),
                testCase(65276, "Asia/Chongqing"),
                testCase(65275, "Asia/Chungking"),
                testCase(65274, "Asia/Colombo"),
                testCase(65273, "Asia/Dacca"),
                testCase(65272, "Asia/Damascus"),
                testCase(65271, "Asia/Dhaka"),
                testCase(65270, "Asia/Dili"),
                testCase(65269, "Asia/Dubai"),
                testCase(65268, "Asia/Dushanbe"),
                testCase(65267, "Asia/Famagusta"),
                testCase(65266, "Asia/Gaza"),
                testCase(65265, "Asia/Harbin"),
                testCase(65264, "Asia/Hebron"),
                testCase(65263, "Asia/Ho_Chi_Minh"),
                testCase(65262, "Asia/Hong_Kong"),
                testCase(65261, "Asia/Hovd"),
                testCase(65260, "Asia/Irkutsk"),
                testCase(65259, "Asia/Istanbul"),
                testCase(65258, "Asia/Jakarta"),
                testCase(65257, "Asia/Jayapura"),
                testCase(65256, "Asia/Jerusalem"),
                testCase(65255, "Asia/Kabul"),
                testCase(65254, "Asia/Kamchatka"),
                testCase(65253, "Asia/Karachi"),
                testCase(65252, "Asia/Kashgar"),
                testCase(65251, "Asia/Kathmandu"),
                testCase(65250, "Asia/Katmandu"),
                testCase(65249, "Asia/Khandyga"),
                testCase(65248, "Asia/Kolkata"),
                testCase(65247, "Asia/Krasnoyarsk"),
                testCase(65246, "Asia/Kuala_Lumpur"),
                testCase(65245, "Asia/Kuching"),
                testCase(65244, "Asia/Kuwait"),
                testCase(65243, "Asia/Macao"),
                testCase(65242, "Asia/Macau"),
                testCase(65241, "Asia/Magadan"),
                testCase(65240, "Asia/Makassar"),
                testCase(65239, "Asia/Manila"),
                testCase(65238, "Asia/Muscat"),
                testCase(65237, "Asia/Nicosia"),
                testCase(65236, "Asia/Novokuznetsk"),
                testCase(65235, "Asia/Novosibirsk"),
                testCase(65234, "Asia/Omsk"),
                testCase(65233, "Asia/Oral"),
                testCase(65232, "Asia/Phnom_Penh"),
                testCase(65231, "Asia/Pontianak"),
                testCase(65230, "Asia/Pyongyang"),
                testCase(65229, "Asia/Qatar"),
                testCase(65228, "Asia/Qyzylorda"),
                testCase(65227, "Asia/Rangoon"),
                testCase(65226, "Asia/Riyadh"),
                testCase(65225, "Asia/Saigon"),
                testCase(65224, "Asia/Sakhalin"),
                testCase(65223, "Asia/Samarkand"),
                testCase(65222, "Asia/Seoul"),
                testCase(65221, "Asia/Shanghai"),
                testCase(65220, "Asia/Singapore"),
                testCase(65219, "Asia/Srednekolymsk"),
                testCase(65218, "Asia/Taipei"),
                testCase(65217, "Asia/Tashkent"),
                testCase(65216, "Asia/Tbilisi"),
                testCase(65215, "Asia/Tehran"),
                testCase(65214, "Asia/Tel_Aviv"),
                testCase(65213, "Asia/Thimbu"),
                testCase(65212, "Asia/Thimphu"),
                testCase(65211, "Asia/Tokyo"),
                testCase(65210, "Asia/Tomsk"),
                testCase(65209, "Asia/Ujung_Pandang"),
                testCase(65208, "Asia/Ulaanbaatar"),
                testCase(65207, "Asia/Ulan_Bator"),
                testCase(65206, "Asia/Urumqi"),
                testCase(65205, "Asia/Ust-Nera"),
                testCase(65204, "Asia/Vientiane"),
                testCase(65203, "Asia/Vladivostok"),
                testCase(65202, "Asia/Yakutsk"),
                testCase(65201, "Asia/Yangon"),
                testCase(65200, "Asia/Yekaterinburg"),
                testCase(65199, "Asia/Yerevan"),
                testCase(65198, "Atlantic/Azores"),
                testCase(65197, "Atlantic/Bermuda"),
                testCase(65196, "Atlantic/Canary"),
                testCase(65195, "Atlantic/Cape_Verde"),
                testCase(65194, "Atlantic/Faeroe"),
                testCase(65193, "Atlantic/Faroe"),
                testCase(65192, "Atlantic/Jan_Mayen"),
                testCase(65191, "Atlantic/Madeira"),
                testCase(65190, "Atlantic/Reykjavik"),
                testCase(65189, "Atlantic/South_Georgia"),
                testCase(65188, "Atlantic/St_Helena"),
                testCase(65187, "Atlantic/Stanley"),
                testCase(65186, "Australia/ACT"),
                testCase(65185, "Australia/Adelaide"),
                testCase(65184, "Australia/Brisbane"),
                testCase(65183, "Australia/Broken_Hill"),
                testCase(65182, "Australia/Canberra"),
                testCase(65181, "Australia/Currie"),
                testCase(65180, "Australia/Darwin"),
                testCase(65179, "Australia/Eucla"),
                testCase(65178, "Australia/Hobart"),
                testCase(65177, "Australia/LHI"),
                testCase(65176, "Australia/Lindeman"),
                testCase(65175, "Australia/Lord_Howe"),
                testCase(65174, "Australia/Melbourne"),
                testCase(65173, "Australia/NSW"),
                testCase(65172, "Australia/North"),
                testCase(65171, "Australia/Perth"),
                testCase(65170, "Australia/Queensland"),
                testCase(65169, "Australia/South"),
                testCase(65168, "Australia/Sydney"),
                testCase(65167, "Australia/Tasmania"),
                testCase(65166, "Australia/Victoria"),
                testCase(65165, "Australia/West"),
                testCase(65164, "Australia/Yancowinna"),
                testCase(65163, "BET", "America/Sao_Paulo"),
                testCase(65162, "BST", "Asia/Dhaka"),
                testCase(65161, "Brazil/Acre"),
                testCase(65160, "Brazil/DeNoronha"),
                testCase(65159, "Brazil/East"),
                testCase(65158, "Brazil/West"),
                testCase(65157, "CAT", "Africa/Harare"),
                testCase(65156, "CET"),
                testCase(65155, "CNT", "America/St_Johns"),
                testCase(65154, "CST", "America/Chicago"),
                testCase(65153, "CST6CDT"),
                testCase(65152, "CTT", "Asia/Shanghai"),
                testCase(65151, "Canada/Atlantic"),
                testCase(65150, "Canada/Central"),
                testCase(65149, "America/Regina"), // was "Canada/East-Saskatchewan" (see also TimeZoneMapping)
                testCase(65148, "Canada/Eastern"),
                testCase(65147, "Canada/Mountain"),
                testCase(65146, "Canada/Newfoundland"),
                testCase(65145, "Canada/Pacific"),
                testCase(65144, "Canada/Saskatchewan"),
                testCase(65143, "Canada/Yukon"),
                testCase(65142, "Chile/Continental"),
                testCase(65141, "Chile/EasterIsland"),
                testCase(65140, "Cuba"),
                testCase(65139, "EAT", "Africa/Addis_Ababa"),
                testCase(65138, "ECT", "Europe/Paris"),
                testCase(65137, "EET"),
                testCase(65136, "EST", "-05:00"),
                testCase(65135, "EST5EDT"),
                testCase(65134, "Egypt"),
                testCase(65133, "Eire"),
                testCase(65132, "Etc/GMT"),
                testCase(65131, "Etc/GMT+0"),
                testCase(65130, "Etc/GMT+1"),
                testCase(65129, "Etc/GMT+10"),
                testCase(65128, "Etc/GMT+11"),
                testCase(65127, "Etc/GMT+12"),
                testCase(65126, "Etc/GMT+2"),
                testCase(65125, "Etc/GMT+3"),
                testCase(65124, "Etc/GMT+4"),
                testCase(65123, "Etc/GMT+5"),
                testCase(65122, "Etc/GMT+6"),
                testCase(65121, "Etc/GMT+7"),
                testCase(65120, "Etc/GMT+8"),
                testCase(65119, "Etc/GMT+9"),
                testCase(65118, "Etc/GMT-0"),
                testCase(65117, "Etc/GMT-1"),
                testCase(65116, "Etc/GMT-10"),
                testCase(65115, "Etc/GMT-11"),
                testCase(65114, "Etc/GMT-12"),
                testCase(65113, "Etc/GMT-13"),
                testCase(65112, "Etc/GMT-14"),
                testCase(65111, "Etc/GMT-2"),
                testCase(65110, "Etc/GMT-3"),
                testCase(65109, "Etc/GMT-4"),
                testCase(65108, "Etc/GMT-5"),
                testCase(65107, "Etc/GMT-6"),
                testCase(65106, "Etc/GMT-7"),
                testCase(65105, "Etc/GMT-8"),
                testCase(65104, "Etc/GMT-9"),
                testCase(65103, "Etc/GMT0"),
                testCase(65102, "Etc/Greenwich"),
                testCase(65101, "Etc/UCT"),
                testCase(65100, "Etc/UTC"),
                testCase(65099, "Etc/Universal"),
                testCase(65098, "Etc/Zulu"),
                testCase(65097, "Europe/Amsterdam"),
                testCase(65096, "Europe/Andorra"),
                testCase(65095, "Europe/Astrakhan"),
                testCase(65094, "Europe/Athens"),
                testCase(65093, "Europe/Belfast"),
                testCase(65092, "Europe/Belgrade"),
                testCase(65091, "Europe/Berlin"),
                testCase(65090, "Europe/Bratislava"),
                testCase(65089, "Europe/Brussels"),
                testCase(65088, "Europe/Bucharest"),
                testCase(65087, "Europe/Budapest"),
                testCase(65086, "Europe/Busingen"),
                testCase(65085, "Europe/Chisinau"),
                testCase(65084, "Europe/Copenhagen"),
                testCase(65083, "Europe/Dublin"),
                testCase(65082, "Europe/Gibraltar"),
                testCase(65081, "Europe/Guernsey"),
                testCase(65080, "Europe/Helsinki"),
                testCase(65079, "Europe/Isle_of_Man"),
                testCase(65078, "Europe/Istanbul"),
                testCase(65077, "Europe/Jersey"),
                testCase(65076, "Europe/Kaliningrad"),
                testCase(65075, "Europe/Kiev"),
                testCase(65074, "Europe/Kirov"),
                testCase(65073, "Europe/Lisbon"),
                testCase(65072, "Europe/Ljubljana"),
                testCase(65071, "Europe/London"),
                testCase(65070, "Europe/Luxembourg"),
                testCase(65069, "Europe/Madrid"),
                testCase(65068, "Europe/Malta"),
                testCase(65067, "Europe/Mariehamn"),
                testCase(65066, "Europe/Minsk"),
                testCase(65065, "Europe/Monaco"),
                testCase(65064, "Europe/Moscow"),
                testCase(65063, "Europe/Nicosia"),
                testCase(65062, "Europe/Oslo"),
                testCase(65061, "Europe/Paris"),
                testCase(65060, "Europe/Podgorica"),
                testCase(65059, "Europe/Prague"),
                testCase(65058, "Europe/Riga"),
                testCase(65057, "Europe/Rome"),
                testCase(65056, "Europe/Samara"),
                testCase(65055, "Europe/San_Marino"),
                testCase(65054, "Europe/Sarajevo"),
                testCase(65053, "Europe/Saratov"),
                testCase(65052, "Europe/Simferopol"),
                testCase(65051, "Europe/Skopje"),
                testCase(65050, "Europe/Sofia"),
                testCase(65049, "Europe/Stockholm"),
                testCase(65048, "Europe/Tallinn"),
                testCase(65047, "Europe/Tirane"),
                testCase(65046, "Europe/Tiraspol"),
                testCase(65045, "Europe/Ulyanovsk"),
                testCase(65044, "Europe/Uzhgorod"),
                testCase(65043, "Europe/Vaduz"),
                testCase(65042, "Europe/Vatican"),
                testCase(65041, "Europe/Vienna"),
                testCase(65040, "Europe/Vilnius"),
                testCase(65039, "Europe/Volgograd"),
                testCase(65038, "Europe/Warsaw"),
                testCase(65037, "Europe/Zagreb"),
                testCase(65036, "Europe/Zaporozhye"),
                testCase(65035, "Europe/Zurich"),
                testCase(65034, "GMT"),
                testCase(65033, "GB"),
                testCase(65032, "GB-Eire"),
                testCase(65031, "GMT+0", "GMT"),
                testCase(65030, "GMT-0", "GMT"),
                testCase(65029, "GMT0"),
                testCase(65028, "Greenwich"),
                testCase(65027, "HST", "-10:00"),
                testCase(65026, "Hongkong"),
                testCase(65025, "IET", "America/Indiana/Indianapolis"),
                testCase(65024, "IST", "Asia/Kolkata"),
                testCase(65023, "Iceland"),
                testCase(65022, "Indian/Antananarivo"),
                testCase(65021, "Indian/Chagos"),
                testCase(65020, "Indian/Christmas"),
                testCase(65019, "Indian/Cocos"),
                testCase(65018, "Indian/Comoro"),
                testCase(65017, "Indian/Kerguelen"),
                testCase(65016, "Indian/Mahe"),
                testCase(65015, "Indian/Maldives"),
                testCase(65014, "Indian/Mauritius"),
                testCase(65013, "Indian/Mayotte"),
                testCase(65012, "Indian/Reunion"),
                testCase(65011, "Iran"),
                testCase(65010, "Israel"),
                testCase(65009, "JST", "Asia/Tokyo"),
                testCase(65008, "Jamaica"),
                testCase(65007, "Japan"),
                testCase(65006, "Kwajalein"),
                testCase(65005, "Libya"),
                testCase(65004, "MET"),
                testCase(65003, "MIT", "Pacific/Apia"),
                testCase(65002, "MST", "-07:00"),
                testCase(65001, "MST7MDT"),
                testCase(65000, "Mexico/BajaNorte"),
                testCase(64999, "Mexico/BajaSur"),
                testCase(64998, "Mexico/General"),
                testCase(64997, "NET", "Asia/Yerevan"),
                testCase(64996, "NST", "Pacific/Auckland"),
                testCase(64995, "NZ"),
                testCase(64994, "NZ-CHAT"),
                testCase(64993, "Navajo"),
                testCase(64992, "PLT", "Asia/Karachi"),
                testCase(64991, "PNT", "America/Phoenix"),
                testCase(64990, "PRC"),
                testCase(64989, "PRT", "America/Puerto_Rico"),
                testCase(64988, "PST", "America/Los_Angeles"),
                testCase(64987, "PST8PDT"),
                testCase(64986, "Pacific/Apia"),
                testCase(64985, "Pacific/Auckland"),
                testCase(64984, "Pacific/Bougainville"),
                testCase(64983, "Pacific/Chatham"),
                testCase(64982, "Pacific/Chuuk"),
                testCase(64981, "Pacific/Easter"),
                testCase(64980, "Pacific/Efate"),
                testCase(64979, "Pacific/Enderbury"),
                testCase(64978, "Pacific/Fakaofo"),
                testCase(64977, "Pacific/Fiji"),
                testCase(64976, "Pacific/Funafuti"),
                testCase(64975, "Pacific/Galapagos"),
                testCase(64974, "Pacific/Gambier"),
                testCase(64973, "Pacific/Guadalcanal"),
                testCase(64972, "Pacific/Guam"),
                testCase(64971, "Pacific/Honolulu"),
                testCase(64970, "Pacific/Johnston"),
                testCase(64969, "Pacific/Kiritimati"),
                testCase(64968, "Pacific/Kosrae"),
                testCase(64967, "Pacific/Kwajalein"),
                testCase(64966, "Pacific/Majuro"),
                testCase(64965, "Pacific/Marquesas"),
                testCase(64964, "Pacific/Midway"),
                testCase(64963, "Pacific/Nauru"),
                testCase(64962, "Pacific/Niue"),
                testCase(64961, "Pacific/Norfolk"),
                testCase(64960, "Pacific/Noumea"),
                testCase(64959, "Pacific/Pago_Pago"),
                testCase(64958, "Pacific/Palau"),
                testCase(64957, "Pacific/Pitcairn"),
                testCase(64956, "Pacific/Pohnpei"),
                testCase(64955, "Pacific/Ponape"),
                testCase(64954, "Pacific/Port_Moresby"),
                testCase(64953, "Pacific/Rarotonga"),
                testCase(64952, "Pacific/Saipan"),
                testCase(64951, "Pacific/Samoa"),
                testCase(64950, "Pacific/Tahiti"),
                testCase(64949, "Pacific/Tarawa"),
                testCase(64948, "Pacific/Tongatapu"),
                testCase(64947, "Pacific/Truk"),
                testCase(64946, "Pacific/Wake"),
                testCase(64945, "Pacific/Wallis"),
                testCase(64944, "Pacific/Yap"),
                testCase(64943, "Poland"),
                testCase(64942, "Portugal"),
                testCase(64941, "Asia/Taipei"),
                testCase(64940, "ROK"),
                testCase(64939, "SST", "Pacific/Guadalcanal"),
                testCase(64938, "Singapore"),
                testCase(64937, "SystemV/AST4"),
                testCase(64936, "SystemV/AST4ADT"),
                testCase(64935, "SystemV/CST6"),
                testCase(64934, "SystemV/CST6CDT"),
                testCase(64933, "SystemV/EST5"),
                testCase(64932, "SystemV/EST5EDT"),
                testCase(64931, "SystemV/HST10"),
                testCase(64930, "SystemV/MST7"),
                testCase(64929, "SystemV/MST7MDT"),
                testCase(64928, "SystemV/PST8"),
                testCase(64927, "SystemV/PST8PDT"),
                testCase(64926, "SystemV/YST9"),
                testCase(64925, "SystemV/YST9YDT"),
                testCase(64924, "Turkey"),
                testCase(64923, "UCT"),
                testCase(64922, "US/Alaska"),
                testCase(64921, "US/Aleutian"),
                testCase(64920, "US/Arizona"),
                testCase(64919, "US/Central"),
                testCase(64918, "US/East-Indiana"),
                testCase(64917, "US/Eastern"),
                testCase(64916, "US/Hawaii"),
                testCase(64915, "US/Indiana-Starke"),
                testCase(64914, "US/Michigan"),
                testCase(64913, "US/Mountain"),
                testCase(64912, "US/Pacific"),
                testCase(64911, "US/Pacific-New"),
                testCase(64910, "US/Samoa"),
                testCase(64909, "UTC"),
                testCase(64908, "Universal"),
                testCase(64907, "VST", "Asia/Ho_Chi_Minh"),
                testCase(64906, "W-SU"),
                testCase(64905, "WET"),
                testCase(64904, "Zulu")
        );
    }

    private static Object[] testCase(int zoneId, String zoneName) {
        return testCase(zoneId, zoneName, null);
    }

    /**
     * Test case data.
     *
     * @param zoneId Firebird zone id
     * @param zoneName Firebird/ICU zone name
     * @param jtZoneId Expected id when mapped to {@link ZoneId}
     * @return Test case
     */
    private static Object[] testCase(int zoneId, String zoneName, String jtZoneId) {
        return new Object[] { zoneId, zoneName, jtZoneId };
    }
}
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

type Dam = {
  id: string;
  code: string;
  name: string;
  description: string | null;
  operationalState: 'NORMAL' | 'WATCH' | 'WARNING' | 'EMERGENCY';
  publicStatusMessage: string | null;
};

type Sensor = {
  id: string;
  name: string;
  sensorType: string;
  unit: string;
  status: 'ACTIVE' | 'DISABLED' | 'MAINTENANCE' | 'OFFLINE';
  lastSeenAt: string | null;
  location: { type: 'Point'; coordinates: [number, number] } | null;
  latestValue: number | null;
  measuredAt: string | null;
  quality: string | null;
};

type Reading = { id: number; measuredAt: string; value: number; quality: string };

const defaultApiUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
const apiUrl = (process.env.EXPO_PUBLIC_API_BASE_URL ?? defaultApiUrl).replace(/\/$/, '');

async function get<T>(path: string): Promise<T> {
  const response = await fetch(`${apiUrl}${path}`, { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error(`Service unavailable (${response.status})`);
  return response.json() as Promise<T>;
}

export default function App() {
  const [dams, setDams] = useState<Dam[]>([]);
  const [damId, setDamId] = useState('');
  const [sensors, setSensors] = useState<Sensor[]>([]);
  const [history, setHistory] = useState<Reading[]>([]);
  const [historySensor, setHistorySensor] = useState<Sensor | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  const loadDams = useCallback(async () => {
    const data = await get<Dam[]>('/api/v1/public/dams');
    setDams(data);
    setDamId((current) => current || data[0]?.id || '');
  }, []);

  const loadSensors = useCallback(async (selectedDam: string) => {
    if (!selectedDam) return;
    setSensors(await get<Sensor[]>(`/api/v1/public/dams/${selectedDam}/sensors`));
  }, []);

  useEffect(() => {
    loadDams().catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false));
  }, [loadDams]);

  useEffect(() => {
    loadSensors(damId).catch((reason: Error) => setError(reason.message));
  }, [damId, loadSensors]);

  async function refresh() {
    setRefreshing(true);
    setError('');
    try {
      await loadDams();
      await loadSensors(damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to refresh');
    } finally {
      setRefreshing(false);
    }
  }

  async function openHistory(sensor: Sensor) {
    setHistorySensor(sensor);
    try {
      const readings = await get<Reading[]>(`/api/v1/public/dams/${damId}/sensors/${sensor.id}/readings?size=24`);
      setHistory(readings.reverse());
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to load sensor history');
      setHistory([]);
    }
  }

  const selectedDam = dams.find((dam) => dam.id === damId);

  if (loading) {
    return <SafeAreaView style={styles.loading}><ActivityIndicator color="#24c58b" size="large"/><Text style={styles.muted}>Loading public dam information…</Text></SafeAreaView>;
  }

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="light" />
      <ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} tintColor="#24c58b" />}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>DDAS PUBLIC</Text>
          <Text style={styles.title}>Dam safety near you</Text>
          <Text style={styles.subtitle}>Verified operational status and public sensor readings.</Text>
        </View>

        {error ? <Pressable style={styles.error} onPress={() => setError('')}><Text style={styles.errorText}>{error} · tap to dismiss</Text></Pressable> : null}

        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.damTabs}>
          {dams.map((dam) => <Pressable key={dam.id} onPress={() => setDamId(dam.id)} style={[styles.damTab, dam.id === damId && styles.damTabActive]}><Text style={[styles.damTabText, dam.id === damId && styles.damTabTextActive]}>{dam.name}</Text></Pressable>)}
        </ScrollView>

        {selectedDam ? <View style={[styles.statusCard, styles[`state${selectedDam.operationalState}`]]}>
          <View style={styles.row}><View style={styles.flex}><Text style={styles.cardLabel}>CURRENT STATUS</Text><Text style={styles.damName}>{selectedDam.name}</Text></View><Text style={styles.state}>{selectedDam.operationalState}</Text></View>
          <Text style={styles.statusMessage}>{selectedDam.publicStatusMessage ?? 'No public safety advisory is currently active.'}</Text>
          {selectedDam.description ? <Text style={styles.description}>{selectedDam.description}</Text> : null}
        </View> : <View style={styles.empty}><Text style={styles.muted}>No public dams are available.</Text></View>}

        <View style={styles.sectionHeader}><View><Text style={styles.cardLabel}>PUBLIC TELEMETRY</Text><Text style={styles.sectionTitle}>Sensor readings</Text></View><Text style={styles.count}>{sensors.length}</Text></View>
        {sensors.map((sensor) => <Pressable key={sensor.id} onPress={() => openHistory(sensor)} style={styles.sensorCard}>
          <View style={styles.row}><View style={styles.flex}><Text style={styles.sensorName}>{sensor.name}</Text><Text style={styles.sensorType}>{sensor.sensorType.replaceAll('_', ' ')}</Text></View><View style={[styles.pill, sensor.status === 'ACTIVE' ? styles.goodPill : styles.warnPill]}><Text style={sensor.status === 'ACTIVE' ? styles.goodText : styles.warnText}>{sensor.status}</Text></View></View>
          <Text style={styles.reading}>{sensor.latestValue ?? '—'} <Text style={styles.unit}>{sensor.unit}</Text></Text>
          <View style={styles.row}><Text style={styles.muted}>{sensor.quality ?? 'Awaiting data'}</Text><Text style={styles.muted}>{sensor.measuredAt ? new Date(sensor.measuredAt).toLocaleTimeString() : 'No recent reading'}</Text></View>
          {sensor.location ? <View style={styles.mapCard}><Text style={styles.mapPin}>●</Text><View><Text style={styles.mapTitle}>Public sensor location</Text><Text style={styles.coordinates}>{sensor.location.coordinates[1].toFixed(4)}, {sensor.location.coordinates[0].toFixed(4)}</Text></View></View> : null}
          <Text style={styles.historyLink}>View allowed history →</Text>
        </Pressable>)}
        {!sensors.length ? <View style={styles.empty}><Text style={styles.muted}>No public sensors are available for this dam.</Text></View> : null}

        {historySensor ? <View style={styles.historyCard}>
          <View style={styles.row}><View style={styles.flex}><Text style={styles.cardLabel}>RECENT HISTORY</Text><Text style={styles.sectionTitle}>{historySensor.name}</Text></View><Pressable onPress={() => setHistorySensor(null)}><Text style={styles.close}>Close</Text></Pressable></View>
          <BarChart readings={history} unit={historySensor.unit}/>
        </View> : null}

        <Text style={styles.footer}>Public data excludes device credentials, maintenance metadata, thresholds, and private sensor locations.</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

function BarChart({ readings, unit }: { readings: Reading[]; unit: string }) {
  if (!readings.length) return <Text style={[styles.muted, styles.chartEmpty]}>No historical readings are available.</Text>;
  const values = readings.map((reading) => Number(reading.value));
  const maximum = Math.max(...values, 1);
  return <View><View style={styles.chart}>{readings.map((reading) => <View key={reading.id} style={[styles.bar, { height: Math.max(5, (Number(reading.value) / maximum) * 110) }]} />)}</View><Text style={styles.chartCaption}>{readings.at(-1)?.value} {unit} latest · {readings.length} public readings</Text></View>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#071916' }, loading: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12, backgroundColor: '#071916' },
  content: { padding: 20, paddingBottom: 48, gap: 18 }, header: { paddingTop: 12, paddingBottom: 4 }, eyebrow: { color: '#39d9a0', fontSize: 12, fontWeight: '800', letterSpacing: 3 }, title: { color: '#f3faf7', fontSize: 32, fontWeight: '700', marginTop: 7 }, subtitle: { color: '#8da8a0', fontSize: 15, lineHeight: 22, marginTop: 8 },
  error: { borderColor: '#a84949', borderWidth: 1, backgroundColor: '#3a1717', borderRadius: 14, padding: 13 }, errorText: { color: '#ffb4b4' },
  damTabs: { gap: 9 }, damTab: { borderColor: '#24453d', borderWidth: 1, borderRadius: 50, paddingHorizontal: 15, paddingVertical: 9 }, damTabActive: { backgroundColor: '#24c58b', borderColor: '#24c58b' }, damTabText: { color: '#9cb2ac', fontWeight: '600' }, damTabTextActive: { color: '#06251b' },
  statusCard: { borderWidth: 1, borderRadius: 24, padding: 20 }, stateNORMAL: { backgroundColor: '#0e3027', borderColor: '#1d6b51' }, stateWATCH: { backgroundColor: '#382d12', borderColor: '#82661e' }, stateWARNING: { backgroundColor: '#402313', borderColor: '#99502a' }, stateEMERGENCY: { backgroundColor: '#411919', borderColor: '#a94141' },
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 10 }, flex: { flex: 1 }, cardLabel: { color: '#78958d', fontSize: 10, fontWeight: '800', letterSpacing: 1.6 }, damName: { color: '#f4faf8', fontSize: 24, fontWeight: '700', marginTop: 3 }, state: { color: '#f4faf8', fontSize: 12, fontWeight: '800' }, statusMessage: { color: '#e5f2ee', fontSize: 16, lineHeight: 23, marginTop: 22 }, description: { color: '#91aaa3', fontSize: 13, lineHeight: 19, marginTop: 9 },
  sectionHeader: { flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between', marginTop: 8 }, sectionTitle: { color: '#f1f7f5', fontSize: 21, fontWeight: '700', marginTop: 3 }, count: { color: '#39d9a0', fontSize: 24, fontWeight: '700' },
  sensorCard: { backgroundColor: '#0d2520', borderColor: '#183d34', borderWidth: 1, borderRadius: 20, padding: 18 }, sensorName: { color: '#f0f8f5', fontSize: 17, fontWeight: '700' }, sensorType: { color: '#708e85', fontSize: 11, marginTop: 3 }, pill: { borderRadius: 50, paddingHorizontal: 9, paddingVertical: 5 }, goodPill: { backgroundColor: '#123f30' }, warnPill: { backgroundColor: '#443310' }, goodText: { color: '#48dda7', fontSize: 10, fontWeight: '800' }, warnText: { color: '#f0bd52', fontSize: 10, fontWeight: '800' }, reading: { color: '#ffffff', fontSize: 38, fontWeight: '700', marginVertical: 16 }, unit: { color: '#88a39b', fontSize: 17, fontWeight: '400' }, muted: { color: '#78938b', fontSize: 12 }, historyLink: { color: '#3bd49e', fontSize: 13, fontWeight: '600', marginTop: 15 },
  mapCard: { flexDirection: 'row', alignItems: 'center', gap: 10, backgroundColor: '#0a1d19', borderRadius: 13, padding: 11, marginTop: 14 }, mapPin: { color: '#39d9a0', fontSize: 20 }, mapTitle: { color: '#cbdcd7', fontSize: 12, fontWeight: '600' }, coordinates: { color: '#657f78', fontSize: 11, marginTop: 2 },
  historyCard: { backgroundColor: '#0d2520', borderColor: '#245344', borderWidth: 1, borderRadius: 22, padding: 19 }, close: { color: '#3bd49e', fontWeight: '600' }, chart: { height: 130, flexDirection: 'row', alignItems: 'flex-end', gap: 3, borderBottomColor: '#28443c', borderBottomWidth: 1, marginTop: 18 }, bar: { flex: 1, maxWidth: 18, backgroundColor: '#2bc58e', borderTopLeftRadius: 3, borderTopRightRadius: 3 }, chartCaption: { color: '#78938b', fontSize: 11, marginTop: 9 }, chartEmpty: { paddingVertical: 30, textAlign: 'center' },
  empty: { borderColor: '#24453d', borderWidth: 1, borderStyle: 'dashed', borderRadius: 18, padding: 28, alignItems: 'center' }, footer: { color: '#58736b', fontSize: 11, lineHeight: 17, textAlign: 'center', marginTop: 8 },
});

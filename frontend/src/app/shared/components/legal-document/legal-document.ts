/**
 * Formato simples dos textos legais guardados no banco (editados pelo administrador):
 * parágrafos separados por linha em branco, "## " título, "### " subtítulo, "- " item de lista,
 * linhas "| a | b |" tabela (a 1ª linha é o cabeçalho), **negrito** e [texto](link).
 *
 * O texto é convertido em blocos e renderizado pelo template do Angular, nunca com innerHTML:
 * assim nenhum HTML ou script digitado no texto chega a ser executado (proteção contra XSS).
 */

export type InlineSegment =
  | { kind: 'text'; text: string }
  | { kind: 'bold'; text: string }
  | { kind: 'link'; text: string; href: string; internal: boolean };

export type LegalBlock =
  | { kind: 'heading'; level: 2 | 3; content: InlineSegment[] }
  | { kind: 'paragraph'; content: InlineSegment[] }
  | { kind: 'list'; items: InlineSegment[][] }
  | { kind: 'table'; header: InlineSegment[][]; rows: InlineSegment[][][] };

const INLINE = /\*\*(.+?)\*\*|\[([^\]]+)\]\(([^)\s]+)\)/g;
const TABLE_SEPARATOR = /^\|[\s|:-]+\|$/;

export function parseInline(text: string): InlineSegment[] {
  const segments: InlineSegment[] = [];
  let last = 0;
  for (const match of text.matchAll(INLINE)) {
    const index = match.index ?? 0;
    if (index > last) {
      segments.push({ kind: 'text', text: text.slice(last, index) });
    }
    if (match[1] !== undefined) {
      segments.push({ kind: 'bold', text: match[1] });
    } else {
      segments.push(toLink(match[2], match[3]));
    }
    last = index + match[0].length;
  }
  if (last < text.length) {
    segments.push({ kind: 'text', text: text.slice(last) });
  }
  return segments;
}

/** Só aceita links internos (/rota), http(s) e mailto; qualquer outro (ex.: javascript:) vira texto. */
function toLink(text: string, href: string): InlineSegment {
  if (href.startsWith('/') && !href.startsWith('//')) {
    return { kind: 'link', text, href, internal: true };
  }
  if (/^(https?:\/\/|mailto:)/i.test(href)) {
    return { kind: 'link', text, href, internal: false };
  }
  return { kind: 'text', text };
}

function cells(line: string): InlineSegment[][] {
  return line
    .trim()
    .replace(/^\|/, '')
    .replace(/\|$/, '')
    .split('|')
    .map((cell) => parseInline(cell.trim()));
}

export function parseLegalDocument(source: string | null | undefined): LegalBlock[] {
  const blocks: LegalBlock[] = [];
  let paragraph: string[] = [];
  let list: InlineSegment[][] | null = null;
  let table: { header: InlineSegment[][]; rows: InlineSegment[][][] } | null = null;

  const flush = () => {
    if (paragraph.length) {
      blocks.push({ kind: 'paragraph', content: parseInline(paragraph.join(' ')) });
      paragraph = [];
    }
    if (list) {
      blocks.push({ kind: 'list', items: list });
      list = null;
    }
    if (table) {
      blocks.push({ kind: 'table', ...table });
      table = null;
    }
  };

  for (const raw of (source ?? '').split(/\r?\n/)) {
    const line = raw.trim();
    if (!line) {
      flush();
    } else if (line.startsWith('### ') || line.startsWith('## ')) {
      flush();
      const level = line.startsWith('### ') ? 3 : 2;
      blocks.push({ kind: 'heading', level, content: parseInline(line.slice(level + 1)) });
    } else if (line.startsWith('- ')) {
      if (!list) {
        flush();
        list = [];
      }
      list.push(parseInline(line.slice(2)));
    } else if (line.startsWith('|')) {
      if (TABLE_SEPARATOR.test(line)) {
        continue;
      }
      if (!table) {
        flush();
        table = { header: cells(line), rows: [] };
      } else {
        table.rows.push(cells(line));
      }
    } else {
      if (list || table) {
        flush();
      }
      paragraph.push(line);
    }
  }
  flush();
  return blocks;
}
